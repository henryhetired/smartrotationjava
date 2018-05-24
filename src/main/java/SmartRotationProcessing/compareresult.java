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
            FileWriter writer = new FileWriter(maskpath + filenamecount);
            FileWriter writer2 = new FileWriter(maskpath + filenameavg);
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
    static void registerlatestImage(String workspace){
        String filename = get_last_mask(workspace);
        if (filename == null){
            System.out.println("This is no image in workspace");
            return;
        }

    }
//    static void run(ImagePlus imp, ImagePlus mask) {
//        float[] projectedImageContainer = sideprojection_entropy(imp);
//        ImageProcessor ip = new FloatProcessor(imp.getHeight(), imp.getStackSize());
//        ip.setPixels(projectedImageContainer);
//        ImagePlus imp_out = new ImagePlus("output", ip);
//        update_mask(mask, imp_out);
//        IJ.saveAs(mask, "tif", maskpath + maskfilename);
//        System.out.println("Mask updated");
//        get_angular_result(mask);
//        save_angular_result("anglecount" + "_" + String.format("%04d", idx) + ".txt", "angleavg" + String.format("%04d", idx) + ".txt");
//
//    }

//    private static void batch_processing(String filepath_base, int num_angles) {
//        rawimageopener opener = new rawimageopener();
//        String path = filepath_base;
//        for (int i = 0; i < num_angles; i++) {
//            String namebase = String.format("%04d", i);
//            maskfilename = "mask" + namebase + ".raw";
//
//            String metaname = "meta" + namebase + ".xml";
//            String filepath = path + namebase + "/view0000/";
//            System.out.println(filepath);
//            String filename = find_raw_img(filepath);
//            curr_filename = filename;
//            String dctname = find_dct_img(filepath);
//            opener.run(filepath + filename);
//            xmlMetadata meta = new xmlMetadata();
//            meta.read(filepath + "meta.xml");
//            //Information about the sample location from rawiamgeopener.java
//            int ystart = meta.samplestartx;
//            int yend = meta.sampleendx;
//            int zstart = meta.samplestartz;
//            int zend = meta.sampleendz;
//            angle = meta.anglepos;
//            xypixelsize = meta.xypixelsize;
//            zpixelsize = meta.zpixelsize;
//            blk_size = meta.blk_size;
//            ///read in the dct image
//            FileInfo fi = new FileInfo();
//            meta.savetofileinfo(fi);
//            fi.height = meta.ImgHeight / meta.blk_size;
//            fi.width = meta.ImgWidth / meta.blk_size;
//            fi.gapBetweenImages = 0;
//            fi.fileType = FileInfo.GRAY32_FLOAT;
//            fi.fileName = dctname;
//            fi.directory = filepath;
//            ImagePlus impdct = new FileOpener(fi).open(false);
////            new ImageJ();
////            imp.show();
//            xmlMetadata maskmeta = new xmlMetadata();
//
//            File f = new File(maskpath + maskfilename);
//            ImagePlus mask;
//
//            if (!f.exists()) {
//                System.out.println("This is the first stack");
//                is_first = true;
//                float[] data = new float[512 * 512];
//                Arrays.fill(data, 20f);
//                ImageProcessor maskip = new FloatProcessor(512, 512, data);
//                mask = new ImagePlus("mask", maskip);
//                maskmeta.ImgHeight = 512;
//                maskmeta.ImgWidth = 512;
//                maskmeta.bitdepth = 32;
//                maskmeta.nImage = 1;
//                maskmeta.create();
//                maskmeta.save(maskpath + metaname);
//
//            } else {
//                System.out.println("Data and mask found");
//                is_first = false;
//                maskmeta.read(maskpath + "meta" + metaname);
//
//                System.out.println(entropybackground);
//                FileInfo maskfi = new FileInfo();
//                mask_width = maskmeta.ImgWidth;
//                mask_height = maskmeta.ImgHeight;
//                maskfi.width = mask_width;
//                maskfi.height = mask_height;
//                maskfi.nImages = 1;
//                maskfi.fileType = fi.fileType;
//                maskfi.intelByteOrder = true;
//                maskfi.fileName = maskfilename;
//                maskfi.directory = maskpath;
//                mask = new FileOpener(maskfi).open(false);
//            }
//
//            entropybackground = maskmeta.entropybackground;
//            angle_reso = maskmeta.ang_reso;
//            ImageStack cropped = impdct.getStack().crop(0, ystart, zstart, impdct.getWidth(), yend - ystart, zend - zstart);
//            ImagePlus croppedimp = new ImagePlus("cropped", cropped);
////        new ImageJ();
////        croppedimp.show();
//
//            run(croppedimp, mask);
//            maskmeta.ImgHeight = mask.getHeight();
//            maskmeta.ImgWidth = mask.getWidth();
//            maskmeta.save(maskpath + metaname);
//
//
//        }
//    }

//    private static void progressive_processing(String filepath) {
//        rawimageopener opener = new rawimageopener();
//        maskfilename = "mask.raw";
//        String metaname = "meta.xml";
//        String filename = find_raw_img(filepath);
//        String dctname = find_dct_img(filepath);
//        opener.run(filepath + filename);
//        xmlMetadata meta = new xmlMetadata();
//        meta.read(filepath + "meta.xml");
//        //Information about the sample location from rawiamgeopener.java
//        int ystart = meta.samplestartx;
//        int yend = meta.sampleendx;
//        int zstart = meta.samplestartz;
//        int zend = meta.sampleendz;
//        angle = meta.anglepos;
//        xypixelsize = meta.xypixelsize;
//        zpixelsize = meta.zpixelsize;
//        blk_size = meta.blk_size;
//        FileInfo fi = new FileInfo();
//        fi.width = meta.ImgWidth / blk_size;
//        fi.height = meta.ImgHeight / blk_size;
//        fi.nImages = meta.nImage;
//        fi.fileType = FileInfo.GRAY32_FLOAT;
//        fi.intelByteOrder = true;
//        fi.fileName = dctname;
//        fi.directory = filepath;
//        ImagePlus imp = new FileOpener(fi).open(false);
//        xmlMetadata maskmeta = new xmlMetadata();
//        File fmask = new File(maskpath + maskfilename);
//        ImagePlus mask;
//        //read or create masks
//        if (!fmask.exists()) {
//            System.out.println("This is the first stack");
//            is_first = true;
//            float[] data = new float[1024 * 1024];
//            Arrays.fill(data, 20f);
//            ImageProcessor maskip = new FloatProcessor(1024, 1024, data);
//            mask = new ImagePlus("mask", maskip);
//            maskmeta.ImgHeight = 1024;
//            maskmeta.ImgWidth = 1024;
//            maskmeta.bitdepth = 32;
//            maskmeta.nImage = 1;
//            maskmeta.create();
//            maskmeta.save(maskpath + metaname);
//            System.out.println(entropybackground);
//
//        } else {
//            System.out.println("Data and mask found");
//            is_first = false;
//            maskmeta.read(maskpath + metaname);
//            FileInfo maskfi = new FileInfo();
//            mask_width = maskmeta.ImgWidth;
//            mask_height = maskmeta.ImgHeight;
//            maskfi.width = mask_width;
//            maskfi.height = mask_height;
//            maskfi.nImages = 1;
//            maskfi.fileType = fi.fileType;
//            maskfi.intelByteOrder = true;
//            maskfi.fileName = maskfilename;
//            maskfi.directory = maskpath;
//            mask = new FileOpener(maskfi).open(false);
//            //test
//        }
//        File fconfig = new File(maskpath + "config.txt");
//        entropybackground = maskmeta.entropybackground;
//        angle_reso = maskmeta.ang_reso;
//        ImageStack cropped = imp.getStack().crop(0, ystart, zstart, imp.getWidth(), yend - ystart, zend - zstart);
//        ImagePlus croppedimp = new ImagePlus("cropped", cropped);
//
//        run(croppedimp, mask);
//        maskmeta.ImgHeight = mask.getHeight();
//        maskmeta.ImgWidth = mask.getWidth();
//        maskmeta.save(maskpath + metaname);
//
//
//    }
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
        update_mask(old_mask,new_mask);
    }
    public static void main(String[] args) {
        //filepath is the location of the image file along with meta.xml
//        String filepath = args[0];
//        String filepath = "/mnt/fileserver/Henry-SPIM/smart_rotation/04052018_corrected/t0000/conf0005/view0000/";
        String filepathbase = "/mnt/fileserver/Henry-SPIM/smart_rotation/04052018_corrected/t0000/";
        //workspace is the location where all the mask/temp is located
//        workspace = args[1];
        workspace = "/mnt/fileserver/Henry-SPIM/smart_rotation/04052018_corrected/workspace/";
//        idx = Integer.parseInt(args[2]);
        System.out.println("Starting analysis ");
        //progressive_processing(filepath);
//        String filepathbase = "/mnt/fileserver/Henry-SPIM/smart_rotation/04052018_corrected/t0000/conf";
//        batch_processing(filepathbase, 24);
//        configwriter cw = new configwriter();
//        try{
//            cw.read("/local/data/");
//            System.out.println(cw.xypixelsize);
//        }
//        catch(IOException e) {
//            e.printStackTrace();
//        }
//        rawimageopenerwithsift ro = new rawimageopenerwithsift();
//
//        ro.run(filepath,workspace);
//        System.out.println(get_last_mask(workspace));
//        progressive_run(filepath,workspace);
        for (int i=0;i<24;i++){
            idx = i;
            String filepath = filepathbase + String.format("conf%04d",i)+"/view0000/";
            progressive_run(filepath,workspace);
        }

    }
}
