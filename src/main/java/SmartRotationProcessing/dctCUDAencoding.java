package SmartRotationProcessing;

import ij.process.FloatProcessor;
import ij.*;
import ij.process.ImageProcessor;
import jcuda.Pointer;
import jcuda.driver.*;

import static jcuda.driver.JCudaDriver.*;


public class dctCUDAencoding {
    public String ptxfilelocation;
    public int blk_size;
    private CUdevice device;
    private CUcontext context;
    public ImagePlus stack;
    public ImagePlus outputdct;
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
    public void dct_encoding_run(){

        if (initialized){
            CUmodule moduledct = new CUmodule();
            cuModuleLoad(moduledct,ptxfilelocation+"encoding.ptx");
            CUfunction dctencodingfunction_v = new CUfunction();
            CUfunction dctencodingfunction_h = new CUfunction();
            cuModuleGetFunction(dctencodingfunction_h,moduledct,"thread_dct_h");
            cuModuleGetFunction(dctencodingfunction_v,moduledct,"thread_dct_v");
            float[][] pixels = new float[stack.getStackSize()][stack.getHeight()*stack.getWidth()];
            for (int i=0;i<stack.getStackSize();i++){
                System.out.println(i);
                pixels[i] = (float[])stack.getStack().getProcessor(i+1).convertToFloatProcessor().getPixels();
                System.out.println(pixels[i][0]);
            }
            System.out.println(pixels.length);
            int array_length = pixels.length;
            int dim1 = stack.getWidth();
            int dim2 = stack.getHeight();
            int plane_length = dim1*dim2;
            int num_blk_col = dim1/blk_size;
            int num_blk_row = dim2/blk_size;
            //////calculate coefficients and copy to device
            calculate_dct_coefficients();
            System.out.println("Coefficients ready");
            CUdeviceptr dctcoefficientsdevice = new CUdeviceptr();
            cuMemAlloc(dctcoefficientsdevice,blk_size*blk_size*4);
            cuMemcpyHtoD(dctcoefficientsdevice,Pointer.to(dctcoefficients),blk_size*blk_size*4);
            System.out.println("Coefficients copied to device");
            //////Allocate device space for input and output
            CUdeviceptr float_image_in = new CUdeviceptr();
            cuMemAlloc(float_image_in,plane_length*4);
            CUdeviceptr dct_image_out = new CUdeviceptr();
            cuMemAlloc(dct_image_out,plane_length*4);
            System.out.println("Space allocaed on device");
            //////Perform encoding and operation

            Pointer next;
            int blk_size_arr[] = new int[1];
            blk_size_arr[0] = blk_size;
            for (int stack_number=0;stack_number<50;stack_number++){
                System.out.println(String.format("Encoding slice %03d",stack_number));
                next = Pointer.to(pixels[stack_number]);
                cuMemcpyHtoD(float_image_in,next,plane_length*4);
                Pointer kernelParameters1 = Pointer.to(Pointer.to(float_image_in),Pointer.to(dctcoefficientsdevice),Pointer.to(dct_image_out),Pointer.to(blk_size_arr));
                Pointer kernelParameters2 = Pointer.to(Pointer.to(dct_image_out),Pointer.to(dctcoefficientsdevice),Pointer.to(float_image_in),Pointer.to(blk_size_arr));
                cuLaunchKernel(dctencodingfunction_h,num_blk_col,num_blk_row,1,blk_size,blk_size,1,0,null,kernelParameters1,null);
                cuCtxSynchronize();
                cuLaunchKernel(dctencodingfunction_v,num_blk_col,num_blk_row,1,blk_size,blk_size,1,0,null,kernelParameters2,null);
                cuCtxSynchronize();
                //cuLaunchKernel(dctencodingfunction_h,num_blk_col,num_blk_row,1,blk_size,blk_size,1,0,null,kernelParameters2,null);
                cuMemcpyDtoH(next,float_image_in,plane_length*4);
                System.out.println(pixels[stack_number][0]);
                cuCtxSynchronize();
            }
            System.out.println("dct success");
            cuMemFree(float_image_in);
            cuMemFree(dct_image_out);
            cuMemFree(dctcoefficientsdevice);
            outputdct = new ImagePlus();
            ImageProcessor ip = new FloatProcessor(stack.getWidth(),stack.getHeight(),pixels[30]);
            outputdct.setProcessor(ip);
            new ImageJ();
            outputdct.show();


        }
    }



}
