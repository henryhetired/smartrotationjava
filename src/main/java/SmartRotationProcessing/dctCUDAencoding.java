package SmartRotationProcessing;

import ij.process.FloatProcessor;
import ij.*;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;
import jcuda.runtime.JCuda;
import jcuda.runtime.cudaStream_t;

import java.io.File;
import java.io.IOException;

import static jcuda.driver.JCudaDriver.*;
import static jcuda.runtime.cudaMemcpyKind.cudaMemcpyHostToDevice;
import static org.apache.commons.io.IOUtils.toByteArray;


public class dctCUDAencoding {
    public String ptxfilelocation;
    public int blk_size;
    private CUdevice device;
    private CUcontext context;
    public ImagePlus stack;
    private ImagePlus outputdct;
    public ImagePlus entropyimg;
    public float[] dctcoefficients;
    private boolean initialized = false;

    public void init_cuda(){
        cuInit(0);
        device = new CUdevice();
        cuDeviceGet(device,0);
        context = new CUcontext();
        cuCtxCreate(context,0,device);
        initialized = true;
    }
    private void calculate_dct_coefficients(){
        int N = blk_size;
        dctcoefficients = new float[blk_size*blk_size];
        for (int j=0;j<N;j++){
            for (int k=0;k<N;k++){
                if (k==0){
                    dctcoefficients[j*N+k]=(float)Math.sqrt(1.0/ (float)N)*(float)Math.cos(Math.PI*(float)(2*k+1)*j/2.0/(float)N);
                }
                else{
                    dctcoefficients[j*N+k]=(float)Math.sqrt(2.0/ (float)N)*(float)Math.cos(Math.PI*(float)(2*k+1)*j/2.0/(float)N);
                }
            }
        }
    }
    public void dct_encoding_run() throws IOException{
        JCuda.setExceptionsEnabled(true);
        if (initialized){
            CUmodule moduledct = new CUmodule();
            //String ptxfile = preparePtxFile("/mnt/isilon/Henry-SPIM/smart_rotation/processingcodes/smartrotationjava/src/main/java/SmartRotationProcessing/encoding.cu");
            String ptxfile = preparePtxFile("/mnt/isilon/Henry-SPIM/smart_rotation/processingcodes/smartrotationjava/src/main/java/SmartRotationProcessing/encoding.ptx");
            System.out.println(ptxfile);
            int result;
            result = cuModuleLoad(moduledct,ptxfile);
            CUfunction dctencodingfunction_v = new CUfunction();
            CUfunction dctencodingfunction_h = new CUfunction();
            CUfunction entropy_calculation = new CUfunction();
            result = cuModuleGetFunction(entropy_calculation,moduledct,"calc_entropy_atomic");
            result = cuModuleGetFunction(dctencodingfunction_h,moduledct,"thread_dct_h");
            result = cuModuleGetFunction(dctencodingfunction_v,moduledct,"thread_dct_v");
            float[][] pixels = new float[stack.getStackSize()][stack.getHeight()*stack.getWidth()];
            System.out.println(pixels[0].length);
            int dim1 = stack.getWidth();
            int dim2 = stack.getHeight();
            int plane_length = dim1*dim2;
            int num_blk_col = dim1/blk_size;
            int num_blk_row = dim2/blk_size;
            //////calculate coefficients and copy to device
            calculate_dct_coefficients();
            System.out.println("Coefficients ready");
            CUdeviceptr dctcoefficientsdevice = new CUdeviceptr();
            result = cuMemAlloc(dctcoefficientsdevice,blk_size*blk_size*Sizeof.FLOAT);
            System.out.println(result);
            result = cuMemcpyHtoD(dctcoefficientsdevice,Pointer.to(dctcoefficients),blk_size*blk_size*Sizeof.FLOAT);
            System.out.println(result);
            System.out.println("Coefficients copied to device");
            //////Allocate device space for input and output
            CUdeviceptr float_image_in = new CUdeviceptr();
            cuMemAlloc(float_image_in,plane_length* Sizeof.FLOAT);
            CUdeviceptr dct_image = new CUdeviceptr();
            cuMemAlloc(dct_image,plane_length*Sizeof.FLOAT);
            System.out.println("Space allocaed on device");
            CUdeviceptr entropy_image_out = new CUdeviceptr();
            cuMemAlloc(entropy_image_out,plane_length/blk_size/blk_size*Sizeof.FLOAT);
            //////Perform encoding and operation
            Pointer next;
            float[][] entropy = new float[stack.getStackSize()][plane_length/blk_size/blk_size];
            for (int i=0;i<stack.getStackSize();i++){
                pixels[i] = (float[])stack.getStack().getProcessor(i+1).convertToFloatProcessor().getPixelsCopy();
            }
            for (int stack_number=0;stack_number<stack.getStackSize();stack_number++) {
                System.out.println(String.format("Encoding slice %03d", stack_number));
                next = Pointer.to(pixels[stack_number]);
                cuMemcpyHtoD(float_image_in, next, plane_length * Sizeof.FLOAT);
                Pointer kernelParameters1 = Pointer.to(Pointer.to(float_image_in), Pointer.to(dctcoefficientsdevice), Pointer.to(dct_image), Pointer.to(new int[]{blk_size}));

                result = cuLaunchKernel(dctencodingfunction_h, num_blk_col, num_blk_row, 1, blk_size, blk_size, 1, 8, null, kernelParameters1, null);

                Pointer kernelParameters2 = Pointer.to(Pointer.to(dct_image), Pointer.to(dctcoefficientsdevice), Pointer.to(float_image_in), Pointer.to(new int[]{blk_size}));
                cuLaunchKernel(dctencodingfunction_v, num_blk_col, num_blk_row, 1, blk_size, blk_size, 1, 0, null, kernelParameters2, null);
                Pointer kernelParameters3 = Pointer.to(Pointer.to(float_image_in),Pointer.to(entropy_image_out),Pointer.to(new int[]{blk_size}));
                cuLaunchKernel(entropy_calculation, num_blk_col, num_blk_row, 1, blk_size, blk_size, 1, 12, null, kernelParameters3, null);
                cuMemcpyDtoH(Pointer.to(entropy[stack_number]),entropy_image_out,plane_length/blk_size/blk_size*Sizeof.FLOAT);
            }
            cuCtxSynchronize();
            System.out.println("dct success");


//            ImageStack outputdctstack = new ImageStack(stack.getWidth(),stack.getHeight(),stack.getStackSize());
//            for (int i=0;i<stack.getStackSize();i++){
//                ImageProcessor ip =new FloatProcessor(stack.getWidth(),stack.getHeight());
//                ip.setPixels(pixeloutput[i]);
//                outputdctstack.setProcessor(ip,i+1);
//            }
//            outputdct = new ImagePlus("dctstack",outputdctstack);
            ////Perform entropy encoding
            System.out.println("Starting entropy encoding");

            System.out.println("Entropy calculation success!");
            ImageStack outputentropystack = new ImageStack(dim1/blk_size,dim2/blk_size,stack.getStackSize());
            for (int i=0;i<stack.getStackSize();i++){
                ImageProcessor ip =new FloatProcessor(dim1/blk_size,dim2/blk_size);
                ip.setPixels(entropy[i]);
                outputentropystack.setProcessor(ip,i+1);
            }
            entropyimg = new ImagePlus("entropy",outputentropystack);
            
            cuMemFree(dct_image);
            cuMemFree(float_image_in);

            cuMemFree(dctcoefficientsdevice);
            new ImageJ();
            //outputdct.show();
            entropyimg.show();
            //IJ.saveAs(outputdct,"tif","test.tif");


        }
    }
    private static String preparePtxFile(String cuFileName) throws IOException
    {
        int endIndex = cuFileName.lastIndexOf('.');
        if (endIndex == -1)
        {
            endIndex = cuFileName.length()-1;
        }
        String ptxFileName = cuFileName.substring(0, endIndex+1)+"ptx";
        File ptxFile = new File(ptxFileName);
        if (ptxFile.exists())
        {
            return ptxFileName;
        }

        File cuFile = new File(cuFileName);
        if (!cuFile.exists())
        {
            throw new IOException("Input file not found: "+cuFileName);
        }
        String modelString = "-m"+System.getProperty("sun.arch.data.model");
        String command =
                "nvcc " + modelString + " -ptx "+
                        cuFile.getPath()+" -o "+ptxFileName;

        System.out.println("Executing\n"+command);
        Process process = Runtime.getRuntime().exec(command);

        String errorMessage =
                new String(toByteArray(process.getErrorStream()));
        String outputMessage =
                new String(toByteArray(process.getInputStream()));
        int exitValue = 0;
        try
        {
            exitValue = process.waitFor();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "Interrupted while waiting for nvcc output", e);
        }

        if (exitValue != 0)
        {
            System.out.println("nvcc process exitValue "+exitValue);
            System.out.println("errorMessage:\n"+errorMessage);
            System.out.println("outputMessage:\n"+outputMessage);
            throw new IOException(
                    "Could not create .ptx file: "+errorMessage);
        }

        System.out.println("Finished creating PTX file");
        return ptxFileName;
    }


}
