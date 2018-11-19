package SmartRotationProcessing;

import ij.IJ;
import ij.ImageJ;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.FileOpener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.IntStream;

import ij.plugin.CanvasResizer;
import ij.process.*;
import ij.ImagePlus;
import org.apache.commons.io.FilenameUtils;

public class SmartRotationProcessing {
    private static String workspace;
    private static float entropybackground = 7.2f;
    private static int angle_reso = 10;
    private static int[] angle_count;
    public static ImagePlus rawImg;
    public static ImagePlus dctImg;
    private static int idx;
    private static int current_timepoint;
    private static boolean usesift = true;
    private static boolean useregistration = true;
    private static float downsamplefactor = 1;
    private static dctCUDAencoding cuda;
    public  boolean initialized = false;
    private static String filepattern;
    private static configwriter config;
    private static boolean evaluated;
    private static int reference_tp;
    static void threshold_entropy(FloatProcessor ip, float max) {
        //function to threshold the entropy
        for (int i = 0; i < ip.getHeight(); i++) {
            for (int j = 0; j < ip.getWidth(); j++) {
                if (ip.getPixelValue(j, i) >= max) {

                    ip.setf(j, i, 20f);
                }
                if (ip.getPixelValue(j,i) ==0f){
                    ip.setf(j,i,20f);
                }
            }
        }
    }

    private static void get_angular_result(ImagePlus img) {
        //the function that calculates the angular foreground count
        angle_count = new int[360 / angle_reso];
        FloatProcessor ip = (FloatProcessor) img.getProcessor();
        int curr_angle;
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                if (ip.getPixelValue(j, i) <= entropybackground && ip.getPixelValue(j, i) > 0f) {
                    curr_angle = (int) (Math.floor(Math.atan(Math.abs((ip.getHeight() / 2.0f - i)) / Math.abs((ip.getWidth() / 2.0f - j))) / Math.PI * 180));

                    if (i <= img.getHeight() / 2) {
                        if (j >= img.getWidth() / 2) {
                            curr_angle = 180 - curr_angle;
                        }
                    } else {
                        if (j <= img.getWidth() / 2) {
                            curr_angle = 360 - curr_angle;
                        } else {
                            curr_angle = 180 + curr_angle;
                        }
                    }
                    angle_count[curr_angle / angle_reso % (360 / angle_reso)]++;
                }
            }
        }
    }

    private static void save_angular_result(String filenamecount) {
        try {
            FileWriter writer = new FileWriter(workspace + filenamecount);
            for (int i = 0; i < angle_count.length; i++) {
                writer.append(Integer.toString(angle_count[i]));
                if (i < angle_count.length - 1) {
                    writer.append(',');
                } else {
                    writer.append('\n');
                }
            }
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }


    private static int count_foreground(ImagePlus img) {
        int count = 0;
        FloatProcessor ip = (FloatProcessor) img.getProcessor();
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                if (ip.getPixelValue(j, i) <= entropybackground) {
                    count++;
                }
            }
        }
        return (count);
    }
    private static String get_last_mask(String workspace){
        File dir = new File(workspace);
        File[] files = dir.listFiles();
        if (files == null || files.length ==0){
            return null;
        }

        File lastmodifiedmaskFile = files[0];
        for (int i=0;i<files.length;i++){
            if (lastmodifiedmaskFile.lastModified() <= files[i].lastModified() && files[i].getName().contains("dct")){
                lastmodifiedmaskFile = files[i];
            }
        }
        return lastmodifiedmaskFile.getName();
    }
    private static String get_last_raw(String workspace){
        File dir = new File(workspace);
        File[] files = dir.listFiles();
        if (files == null || files.length ==0){
            return null;
        }
        File lastmodifiedmaskFile = files[0];
        for (int i=0;i<files.length;i++){
            if(idx>0){
                if (files[i].lastModified()>=lastmodifiedmaskFile.lastModified() && !files[i].getName().contains("dct.tif")){
                    lastmodifiedmaskFile = files[i];
                }
            }
            else{
                if (!files[i].getName().contains("dct.tif")){
                    lastmodifiedmaskFile = files[i];
                }
            }

        }
        return lastmodifiedmaskFile.getName();
    }
     void evaluation_run(String filepath){
        //run steps for all the evaluation steps
        cudaEncode();
        rawimageopenerwithsift rows = new rawimageopenerwithsift();
        rows.init(filepath,workspace,rawImg,dctImg,config);
        rows.run();
        String latestmask = get_last_mask(workspace); //latest reference of the DCT mask
        String latestraw = get_last_raw(workspace);//latest reference MIP
        System.out.println("latestest image is "+ latestraw);
        System.out.println("latest mask is"+latestmask);
        if (idx==0){
            System.out.println("This is the first stack");
                ImagePlus og= rows.rawImage;
                ImagePlus new_dct_transformed = rows.dctImage;
            if (useregistration) {
                image_registration ir = new image_registration();
                ir.use_SIFT = false;

                og = ir.run(og, og);

                new_dct_transformed.setProcessor(ir.applymapping(new_dct_transformed));
            }
            IJ.saveAs(og,"tif",workspace+latestraw);
            get_angular_result(new_dct_transformed);
            save_angular_result(String.format("angularcount%04d_%04d.txt",current_timepoint,idx));
            IJ.saveAs(new_dct_transformed,"tif",workspace+latestmask);
            return;
        }
        else{
            ImagePlus new_mask = rows.dctImage;
            ImagePlus old_raw = new ImagePlus(workspace+String.format(config.filepattern,current_timepoint,idx-1));
            ImagePlus new_raw = rows.rawImage;
            ImagePlus new_dct_transformed = new ImagePlus();
            if (useregistration) {
                image_registration ir = new image_registration();
                ir.downsamplingfactor=downsamplefactor;
                ir.use_SIFT = usesift;
                new_raw = ir.run(old_raw, new_raw);
                new_dct_transformed.setProcessor(ir.applymapping(new_mask));
            }
            else{
                new_dct_transformed.setProcessor(new_mask.getProcessor());
            }
            IJ.saveAs(new_raw,"tif",workspace+latestraw);
            get_angular_result(new_dct_transformed);
            save_angular_result(String.format("angularcount%04d_%04d.txt",current_timepoint,idx));
            IJ.saveAs(new_dct_transformed,"tif",workspace+latestmask);
        }
        return;
    }
    public void evaluation_step(String filepath,int timepoint,int num_angles,int gap){

        //workspace is the location where all the mask/temp is located
        //Evaluate all the images at timepoint with num_angles number of angles
        System.out.println("Starting analysis:");
        for (int i=0;i<num_angles;i+=gap) {
            idx = i;
            String filename = filepath + String.format(config.filepattern,timepoint,idx);
            open_image(filename); //open either tif or raw files
//            IJ.saveAs(dctImg,"tiff",workspace+String.format("t0000_conf%04d_view0000_c00_dct.tif",i));
            evaluation_run(filename);
        }
        analysiswithsift as = new analysiswithsift();
        as.generate_rainbow_plot(workspace,num_angles,gap);
        reference_tp = current_timepoint;
    }
    public  void update_step(String filepath,int angle_idx,int timepoint){
        //function to create the evaluation of a specific view at a specific timepoint

        if (evaluated) {
            /*open and encode image*/
            String filename = filepath + String.format(config.filepattern, timepoint, angle_idx);
            open_image(filename);
            cudaEncode();
            rawimageopenerwithsift rows = new rawimageopenerwithsift();
            rows.init(filepath, workspace, rawImg, dctImg, config);
            rows.run();
            /*grab the latest projection images just generated*/
            ImagePlus new_mask = rows.dctImage;
            ImagePlus old_raw = new ImagePlus(workspace+String.format(config.filepattern,reference_tp,idx));
            ImagePlus new_raw = rows.rawImage;
            ImagePlus new_dct_transformed = new ImagePlus();
            if (useregistration) {
                image_registration ir = new image_registration();
                ir.downsamplingfactor=downsamplefactor;
                ir.use_SIFT = usesift;
                new_raw = ir.run(old_raw, new_raw);
                new_dct_transformed.setProcessor(ir.applymapping(new_mask));
            }
            else{
                new_dct_transformed.setProcessor(new_mask.getProcessor());
            }
            IJ.saveAs(new_raw,"tif",workspace+String.format(config.filepattern,timepoint,angle_idx));
            get_angular_result(new_dct_transformed);
            save_angular_result(String.format("angularcount%04d_%04d.txt",current_timepoint,idx));
            IJ.saveAs(new_dct_transformed,"tif",workspace+FilenameUtils.getBaseName(filename)+"_dct.tif");
            System.out.println("Evaluation completed");
        }
        else{
            System.out.println("No evaluation step has been performed, terminating");
            return;
        }
    }
    private void cudaEncode(){
        if (initialized){
            long start_time = System.currentTimeMillis();
            long tp1 = System.currentTimeMillis() - start_time;
            System.out.println("File reading time is " + tp1 + " ms");
            if (rawImg.getStackSize()>500){
                downsamplefactor = 1.6f;
            }
            else{
                downsamplefactor = 1f;
            }
            cuda.stack = rawImg;
            try{cuda.dct_encoding_run();}
            catch (IOException e){
                e.printStackTrace();
            }
            long tp2 = System.currentTimeMillis() - start_time;
            System.out.println("Encoding time is " + (tp2-tp1) + " ms");
            dctImg = cuda.entropyimg;
            evaluated = true;
        }
    }
    private static void open_image(String filename){
        if (filename.endsWith("tif")){
            rawImg = new ImagePlus(filename);
        }
        else if (filename.endsWith("raw")){
            String filenamebase = FilenameUtils.removeExtension(filename);
            ImgMetadata meta = new ImgMetadata();
            try{meta.read(filenamebase+"txt");}
            catch (IOException e){
                e.printStackTrace();
            }
            FileInfo fi = new FileInfo();
            fi.fileType = FileInfo.GRAY16_UNSIGNED;
            fi.fileName = filename;
            fi.width = meta.ImgWidth;
            fi.height = meta.ImgHeight;
            fi.nImages = meta.nImage;
            fi.gapBetweenImages = meta.gapbetweenimages;
            rawImg = new FileOpener(fi).open(false);
        }
    }
    public  void init(String workspacein){
        //initialize cuda device and prep
        workspace = workspacein;
        cuda = new dctCUDAencoding();
        cuda.init_cuda();
        //default registration parameters
        useregistration = true;
        usesift = true;
        initialized = true;
        String configname = workspace + "config.txt";
        config = new configwriter();
        try{config.read(configname);}
        catch (IOException e){
            e.printStackTrace();
        }
        cuda.blk_size = config.blk_size;
        cuda.ptxfilelocation = workspace;
        angle_reso = config.ang_reso;
        idx = 0;
        current_timepoint = 0;
        reference_tp = 0;
    }
    public static void main(String[] args) {
//        filepath is the location of the image file along with meta.xml
//        String filepath = args[0];
//        workspace = args[1];
//        init();
//        if (initialized){
//            if (args[2] == "evaluation"){
//                int gap = 1;
//                int num_angles = Integer.parseInt(args[3]);
//                int timepoint = Integer.parseInt(args[4]);
//                entropybackground = 6.5f;
//                evaluation_step(num_angles,filepath,gap);
//            }
//        }
//        configwriter cw = new configwriter();
//        try{cw.read("/mnt/fileserver/Henry-SPIM/");}
//        catch (IOException e){
//            e.printStackTrace();
//        }
//        System.out.println(String.format(cw.filepattern,4,4));
        TCPserver runserver = new TCPserver();
        runserver.init();
        runserver.run();


//        for (int i=0;i<angle_updated.length;i++){
//            System.out.println(angle_updated[i]);
//        }
//        String filepath = args[0];
//
//        try{processingserver ps = new processingserver(53705);}
//        catch (IOException i){
//            i.printStackTrace();
//        }
//        long start_time = System.currentTimeMillis();
//        pythonevaluation pt = new pythonevaluation();
//        pt.pythonlocation = "/home/henryhe/anaconda3/bin/python";
//        pt.scriptlocation = "/mnt/fileserver/Henry-SPIM/smart_rotation/python/evaluationstep.py";
//        pt.pycalltest("/mnt/fileserver/Henry-SPIM/smart_rotation/06142018/sample1/merged/workspace/angularcount/",24,10);
//        System.out.println("Runtime is " + (System.currentTimeMillis() - start_time) + " ms");
//
//        workspace = "/mnt/isilon/Henry-SPIM/smart_rotation/06142018/sample1/merged/c00/";
//        String workspaceorigin = workspace;
//        entropybackground = 7.11f;
//        analysiswithsift ir = new analysiswithsift();
//        ir.generate_comparison(workspace);
//        for (int i=4;i<5;i++){
//            workspace = workspaceorigin;
//            String workspacetemp = workspace+Integer.toString(i)+"angles/";
//            workspace = workspacetemp;
//            ImagePlus img = new ImagePlus(workspacetemp+"dct_resized.tif");
//            for (int j=0;j<img.getStackSize();j++){
//                ImageProcessor ip = img.getStack().getProcessor(j+1);
//                ImagePlus temp = new ImagePlus();
//                temp.setProcessor(ip);
//                get_angular_result(temp);
//                save_angular_result(String.format("angularcount%02d.txt",j));
//            }
//        }


    }
}
