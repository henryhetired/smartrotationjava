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

import static SmartRotationProcessing.rawimageopenerwithsift.find_dct_img;
import static SmartRotationProcessing.rawimageopenerwithsift.find_raw_img;

public class compareresult {
    private static String maskpath = "Z:\\Henry-SPIM\\11132017\\e2\\t0000\\";
    private static String curr_filename;
    private static String workspace;
    private static String maskfilename;
    private static int mask_width;
    private static int mask_height;
    private static double xypixelsize = 0.65;
    private static double zpixelsize = 2;
    private static int blk_size = 16;
    private static double angle;
    private static float entropybackground = 7.2f;
    private static int angle_reso = 10;
    private static int[] angle_count;
    private static float[] angle_avg;
    private static boolean is_first = true;
    private static int idx;
    private static int out[][];


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

    static void get_angular_result(ImagePlus img) {
        //the function that calculates the angular foreground count
        angle_count = new int[360 / angle_reso];
        angle_avg = new float[360 / angle_reso];
        FloatProcessor ip = (FloatProcessor) img.getProcessor();
        int curr_angle = 0;
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
                    angle_avg[curr_angle / angle_reso % (360 / angle_reso)] += ip.getPixelValue(j, i);
                }
            }
        }
        for (int i = 0; i < angle_count.length; i++) {
            angle_avg[i] = angle_avg[i] / angle_count[i];
        }
    }

    static void save_angular_result(String filenamecount, String filenameavg) {
        try {
            FileWriter writer = new FileWriter(workspace + filenamecount);
            FileWriter writer2 = new FileWriter(workspace + filenameavg);
            for (int i = 0; i < angle_count.length; i++) {
                writer.append(Integer.toString(angle_count[i]));
                writer2.append(Float.toString(angle_avg[i]));
                if (i < angle_count.length - 1) {
                    writer.append(',');
                    writer2.append(',');
                } else {
                    writer.append('\n');
                    writer2.append('\n');
                }
            }
            writer.close();
            writer2.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    static void update_mask(ImagePlus old_mask, ImagePlus new_mask) {
        //get a minimum intensity projection between old_mask and new_mask to get a updated mask and store it in mask.tif in workspace
        FloatProcessor old_data = (FloatProcessor) old_mask.getProcessor();
        threshold_entropy(old_data,entropybackground);
        FloatProcessor new_data = (FloatProcessor) new_mask.getProcessor();
        threshold_entropy(new_data,entropybackground);
        for (int i=0;i<old_data.getHeight();i++){
            for (int j=0;j<old_data.getWidth();j++){
                if (new_data.getPixelValue(j,i)<old_data.getPixelValue(j,i)){
                    old_data.setf(j,i,new_data.getPixelValue(j,i));
                }
            }
        }
        ImagePlus updated_mask = new ImagePlus();
        updated_mask.setProcessor(old_data);
        IJ.saveAs(updated_mask,"tif",workspace+"maskdct.tif");
        IJ.saveAs(updated_mask,"tif",workspace+"maskdct"+String.format("%02d",idx)+".tif");
        get_angular_result(updated_mask);
        save_angular_result(String.format("angularcountcumulative%04d.txt",idx),String.format("angularavgcumulative%04d.txt",idx));

    }

    static int count_foreground(ImagePlus img) {
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
    static String get_last_mask(String workspace){
        File dir = new File(workspace);
        File[] files = dir.listFiles();
        if (files == null || files.length ==0){
            return null;
        }

        File lastmodifiedmaskFile = files[0];
        for (int i=1;i<files.length;i++){
            if (lastmodifiedmaskFile.lastModified() < files[i].lastModified() && files[i].getName().contains("dct")){
                lastmodifiedmaskFile = files[i];
            }
        }
        return lastmodifiedmaskFile.getName();
    }
    static String get_last_raw(String workspace){
        File dir = new File(workspace);
        File[] files = dir.listFiles();
        if (files == null || files.length ==0){
            return null;
        }

        File lastmodifiedmaskFile = files[0];
        for (int i=1;i<files.length;i++){
            if (lastmodifiedmaskFile.lastModified() < files[i].lastModified() && !files[i].getName().contains("dct")){
                lastmodifiedmaskFile = files[i];
            }
        }
        return lastmodifiedmaskFile.getName();
    }
    static void progressive_run(String filepath,String workspace){
        rawimageopenerwithsift rows = new rawimageopenerwithsift();
        rows.run(filepath,workspace);
        File maskdct = new File(workspace+"maskdct.tif");
        File maskraw = new File(workspace+"maskraw.tif");
        String latestmask = get_last_mask(workspace);
        String latestraw = get_last_raw(workspace);
        if (!maskdct.exists()&&!maskraw.exists()){
            System.out.println("This is the first stack");
            try {
                Files.copy(Paths.get(workspace + latestmask), Paths.get(workspace + "maskdct.tif"));
                Files.copy(Paths.get(workspace + latestmask), Paths.get(workspace + "maskdct00.tif"));
                Files.copy(Paths.get(workspace + latestraw), Paths.get(workspace + "maskraw.tif"));
            }
            catch (IOException e){
                e.printStackTrace();
            }
            ImagePlus img = new ImagePlus(workspace+"maskdct00.tif");
            get_angular_result(img);
            save_angular_result("angularcount0000.txt","angularaverage0000.txt");
            return;
        }
        ImagePlus old_mask = new ImagePlus(workspace+"maskdct.tif");
        ImagePlus new_mask = new ImagePlus(workspace+latestmask);
        ImagePlus old_raw = new ImagePlus(workspace+"maskraw.tif");
        ImagePlus new_raw = new ImagePlus(workspace+latestraw);
        image_registration ir = new image_registration();
        new_raw = ir.run(old_raw,new_raw);
        IJ.saveAs(new_raw,"tif",workspace+"maskraw.tif");
        IJ.saveAs(new_raw,"tif",workspace+latestraw);
        ImagePlus new_dct_transformed = new ImagePlus();
        new_dct_transformed.setProcessor(ir.applymapping(new_mask));
        get_angular_result(new_dct_transformed);
        save_angular_result(String.format("angularcount%04d.txt",idx),String.format("angularaverage%04d.txt",idx));
        IJ.saveAs(new_dct_transformed,"tif",workspace+latestmask);
        update_mask(old_mask,new_dct_transformed);
    }
    public static void main(String[] args) {
        //filepath is the location of the image file along with meta.xml
////        String filepath = args[0];
////        String filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/04052018_corrected/t0000/conf0005/view0000/";
//        String filepathbase = "/mnt/isilon/Henry-SPIM/smart_rotation/04052018_correct/corrected/corrected_raw/t0000/";
//        //workspace is the location where all the mask/temp is located
////        workspace = args[1];
//        workspace = "/mnt/isilon/Henry-SPIM/smart_rotation/04052018_correct/corrected/corrected_raw/workspace/";
//        System.out.println("Starting analysis ");
//        for (int i=0;i<24;i++) {
//            idx = i;
//            long start_time = System.currentTimeMillis();
//            String filepath = filepathbase + String.format("conf%04d", i);
//            progressive_run(filepath, workspace);
//            System.out.println("Runtime is " + (System.currentTimeMillis() - start_time) + " ms");
//
//        }
        dctCUDAencoding cuda = new dctCUDAencoding();
        cuda.init_cuda();
        cuda.blk_size = 16;
        cuda.ptxfilelocation = "/mnt/isilon/Henry-SPIM/smart_rotation/processingcodes/smartrotationjava/src/main/java/SmartRotationProcessing/";
        ImagePlus ip = new ImagePlus("/mnt/isilon/Henry-SPIM/06112018_richard/e5/data/t0059_conf0000_view0000_view0000.ome.tif");
        cuda.stack = ip;
        try{cuda.dct_encoding_run();}
        catch (IOException e){
            e.printStackTrace();
        }
        }
}
