package SmartRotationProcessing;

import ij.IJ;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.FileOpener;
import java.io.*;
import java.util.Arrays;
import java.util.stream.IntStream;

import ij.plugin.CanvasResizer;
import ij.process.*;
import ij.ImagePlus;

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

    static void combinationUtil(int arr[], int n, int r, int index, int data[], int i) {
        // Current combination is ready to be printed, print it
        if (index == r) {
            for (int j = 0; j < r; j++) {
                System.out.print(data[j] + " ");
                out[idx][j] = data[j];
            }
            System.out.println("");
            idx++;
            return;
        }
        // When no more elements are there to put in data[]
        if (i >= n)
            return;

        // current is included, put next at next location
        data[index] = arr[i];
        combinationUtil(arr, n, r, index + 1, data, i + 1);

        // current is excluded, replace it with next (Note that
        // i+1 is passed, but index is not changed)
        combinationUtil(arr, n, r, index, data, i + 1);
    }

    static int binomi(int n, int k) {
        if ((n == k) || (k == 0))
            return 1;
        else
            return binomi(n - 1, k) + binomi(n - 1, k - 1);
    }

    static void threshold_entropy(FloatProcessor ip, float max) {
        //function to threshold the entropy
        for (int i = 0; i < ip.getHeight(); i++) {
            for (int j = 0; j < ip.getWidth(); j++) {
                if (ip.getPixelValue(j, i) >= max) {

                    ip.setf(j, i, 20f);
                }
            }
        }
    }

    static float[] sideprojection_entropy(ImagePlus imp) {

        //////////////////////////////////////////
        //getpixels // Editing pixel at x,y position new_pixels[y * width + x] = ...;
        //new implementation with direct access to data within the container
        //also parallelize the stack operation since they are independent
        int stacksize = imp.getStackSize();
        int height = imp.getHeight();
        int width = imp.getWidth();
        float[] imagecontainer = new float[stacksize * height];
        IntStream.range(0, stacksize).parallel().forEach(ziter -> {
            FloatProcessor ip = (FloatProcessor) imp.getStack().getProcessor(ziter + 1);
            for (int yiter = 0; yiter < height; yiter++) {
                float maxvalue = 100f;
                for (int xiter = 0; xiter < width; xiter++) {
                    maxvalue = Math.min(maxvalue, ip.getPixelValue(xiter, yiter));
                }
                imagecontainer[ziter * height + yiter] = maxvalue;

            }
        });


        return imagecontainer;
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
        //get a maximum intensity projection between old_mask and new_mask to get a updated mask and store it in old_mask
        FloatProcessor old_data = (FloatProcessor) old_mask.getProcessor();
        FloatProcessor new_data = (FloatProcessor) new_mask.getProcessor();
        new_data.setInterpolationMethod(ImageProcessor.NONE);
        new_data = (FloatProcessor) new_data.resize((int) Math.floor(new_data.getWidth() * blk_size * xypixelsize), (int) Math.floor(new_data.getHeight() * zpixelsize), true);
        if (!is_first) {
            //if not the first stack, need to pad the image first
            int maxwidth = (int) Math.ceil(Math.max(old_data.getWidth(), new_data.getWidth()) / blk_size) * blk_size;
            int maxheight = (int) Math.ceil(Math.max(old_data.getHeight(), new_data.getHeight()) / blk_size) * blk_size;
            CanvasResizer cr = new CanvasResizer();
            FloatProcessor oldmaskFP = (FloatProcessor) cr.expandImage(old_data, maxwidth, maxheight, (maxwidth - old_data.getWidth()) / 2, (maxheight - old_data.getHeight()) / 2);
            FloatProcessor newmaskFP = (FloatProcessor) cr.expandImage(new_data, maxwidth, maxheight, (maxwidth - new_data.getWidth()) / 2, (maxheight - new_data.getHeight()) / 2);
            newmaskFP.setBackgroundValue(entropybackground);
            FloatProcessor newmaskFPatOriginalSize = (FloatProcessor) newmaskFP.resize(newmaskFP.getWidth() / blk_size, newmaskFP.getHeight() / blk_size);
            newmaskFPatOriginalSize.rotate(-angle); //to avoid interpolation artefacts
            newmaskFP = (FloatProcessor) newmaskFPatOriginalSize.resize(newmaskFP.getWidth(), newmaskFP.getHeight());


            for (int i = 0; i < oldmaskFP.getHeight(); i++) {
                for (int j = 0; j < oldmaskFP.getWidth(); j++) {
                    if (oldmaskFP.getPixelValue(j, i) > newmaskFP.getPixelValue(j, i)) {
                        oldmaskFP.setf(j, i, newmaskFP.getPixelValue(j, i));
                    }
                    if (oldmaskFP.getPixelValue(j, i) <= 0.0f) {
                        oldmaskFP.setf(j, i, 20f);
                    }
                }

            }
            compareresult.threshold_entropy(oldmaskFP, entropybackground);

            old_mask.setProcessor(oldmaskFP);
        } else {
            int maxwidth = (int) Math.ceil(Math.max(new_data.getHeight(), new_data.getWidth()) / blk_size) * blk_size;
            int maxheight = (int) Math.ceil(Math.max(new_data.getHeight(), new_data.getWidth()) / blk_size) * blk_size;
            CanvasResizer cr = new CanvasResizer();
            FloatProcessor newmaskFP = (FloatProcessor) cr.expandImage(new_data, maxwidth, maxheight, (maxwidth - new_data.getWidth()) / 2, (maxheight - new_data.getHeight()) / 2);
            newmaskFP.setBackgroundValue(entropybackground);
            compareresult.threshold_entropy(newmaskFP, entropybackground);
            FloatProcessor newmaskFPatOriginalSize = (FloatProcessor) newmaskFP.resize(newmaskFP.getWidth() / blk_size, newmaskFP.getHeight() / blk_size);
            newmaskFPatOriginalSize.rotate(angle);
            newmaskFP = (FloatProcessor) newmaskFPatOriginalSize.resize(newmaskFP.getWidth(), newmaskFP.getHeight());
            FloatProcessor oldmaskFP = newmaskFP;

            for (int i = 0; i < oldmaskFP.getHeight(); i++) {
                for (int j = 0; j < oldmaskFP.getWidth(); j++) {
                    if (oldmaskFP.getPixelValue(j, i) <= 0.0f) {
                        oldmaskFP.setf(j, i, 20f);
                    }
                }
            }
            old_mask.setProcessor(oldmaskFP);
        }
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

    static void run(ImagePlus imp, ImagePlus mask) {
        float[] projectedImageContainer = sideprojection_entropy(imp);
        ImageProcessor ip = new FloatProcessor(imp.getHeight(), imp.getStackSize());
        ip.setPixels(projectedImageContainer);
        ImagePlus imp_out = new ImagePlus("output", ip);
        update_mask(mask, imp_out);
        IJ.saveAs(mask, "raw", maskpath + maskfilename);
        System.out.println("Mask updated");
        get_angular_result(mask);
        save_angular_result("anglecount" + "_"+String.format("%04d", idx) + ".txt", "angleavg" + String.format("%04d", idx) + ".txt");

    }

    static String find_raw_img(String filepath) {
        //assuming the largest file in the folder is the raw imagefile (safe bet isn't it)
        File folder = new File(filepath);
        File[] listoffiles = folder.listFiles();
        int maxat = 0;
        for (int i = 0; i < listoffiles.length; i++) {
            if (listoffiles[i].isFile()) {
                maxat = listoffiles[i].length() > listoffiles[maxat].length() ? i : maxat;
            }
        }
        return listoffiles[maxat].getName();
    }

    static String find_dct_img(String filepath) {
        File folder = new File(filepath);
        File[] listoffiles = folder.listFiles();
        int idx = 0;
        for (int i = 0; i < listoffiles.length; i++) {
            if (listoffiles[i].isFile()) {
                idx = listoffiles[i].getName().contains("dct") ? i : idx;
            }
        }
        return listoffiles[idx].getName();
    }

    private static void batch_processing(String filepath_base, int num_angles) {
        rawimageopener opener = new rawimageopener();
        String path = filepath_base;
        for (int i = 0; i < num_angles; i++) {
            String namebase = String.format("%04d", i);
            maskfilename = "mask" + namebase + ".raw";

            String metaname = "meta" + namebase + ".xml";
            String filepath = path + namebase + "\\view0000\\";

            String filename = find_raw_img(filepath);
            curr_filename = filename;
            String dctname = find_dct_img(filepath);
            opener.run(filepath + filename);
            xmlMetadata meta = new xmlMetadata();
            meta.read(filepath + "meta.xml");
            //Information about the sample location from rawiamgeopener.java
            int ystart = meta.samplestartx;
            int yend = meta.sampleendx;
            int zstart = meta.samplestartz;
            int zend = meta.sampleendz;
            angle = meta.anglepos;
            //Caution: angle needed to rotate the image needs to be adjusted for pixel size
            xypixelsize = meta.xypixelsize;
            zpixelsize = meta.zpixelsize;
            blk_size = meta.blk_size;
            FileInfo fi = new FileInfo();
            fi.width = meta.ImgWidth / blk_size;
            fi.height = meta.ImgHeight / blk_size;
            fi.nImages = meta.nImage;
            fi.fileType = FileInfo.GRAY32_FLOAT;
            fi.intelByteOrder = true;
            fi.fileName = dctname;
            fi.directory = filepath;
            ImagePlus imp = new FileOpener(fi).open(false);
//        new ImageJ();
//        imp.show();
            xmlMetadata maskmeta = new xmlMetadata();

            File f = new File(maskpath + maskfilename);
            ImagePlus mask;

            if (!f.exists()) {
                System.out.println("This is the first stack");
                is_first = true;
                float[] data = new float[512 * 512];
                Arrays.fill(data, 20f);
                ImageProcessor maskip = new FloatProcessor(512, 512, data);
                mask = new ImagePlus("mask", maskip);
                maskmeta.ImgHeight = 512;
                maskmeta.ImgWidth = 512;
                maskmeta.bitdepth = 32;
                maskmeta.nImage = 1;
                maskmeta.create();
                maskmeta.save(maskpath + metaname);

            } else {
                System.out.println("Data and mask found");
                is_first = false;
                maskmeta.read(maskpath + "meta" + metaname);

                System.out.println(entropybackground);
                FileInfo maskfi = new FileInfo();
                mask_width = maskmeta.ImgWidth;
                mask_height = maskmeta.ImgHeight;
                maskfi.width = mask_width;
                maskfi.height = mask_height;
                maskfi.nImages = 1;
                maskfi.fileType = fi.fileType;
                maskfi.intelByteOrder = true;
                maskfi.fileName = maskfilename;
                maskfi.directory = maskpath;
                mask = new FileOpener(maskfi).open(false);
            }

            entropybackground = maskmeta.entropybackground;
            angle_reso = maskmeta.ang_reso;
            ImageStack cropped = imp.getStack().crop(0, ystart, zstart, imp.getWidth(), yend - ystart, zend - zstart);
            ImagePlus croppedimp = new ImagePlus("cropped", cropped);
//        new ImageJ();
//        croppedimp.show();

            run(croppedimp, mask);
            maskmeta.ImgHeight = mask.getHeight();
            maskmeta.ImgWidth = mask.getWidth();
            maskmeta.save(maskpath + metaname);


        }
    }

    private static void progressive_processing(String filepath) {
        rawimageopener opener = new rawimageopener();
        maskfilename = "mask.raw";
        String metaname = "meta.xml";
        String filename = find_raw_img(filepath);
        String dctname = find_dct_img(filepath);
        opener.run(filepath + filename);
        xmlMetadata meta = new xmlMetadata();
        meta.read(filepath + "meta.xml");
        //Information about the sample location from rawiamgeopener.java
        int ystart = meta.samplestartx;
        int yend = meta.sampleendx;
        int zstart = meta.samplestartz;
        int zend = meta.sampleendz;
        angle = meta.anglepos;
        xypixelsize = meta.xypixelsize;
        zpixelsize = meta.zpixelsize;
        blk_size = meta.blk_size;
        FileInfo fi = new FileInfo();
        fi.width = meta.ImgWidth / blk_size;
        fi.height = meta.ImgHeight / blk_size;
        fi.nImages = meta.nImage;
        fi.fileType = FileInfo.GRAY32_FLOAT;
        fi.intelByteOrder = true;
        fi.fileName = dctname;
        fi.directory = filepath;
        ImagePlus imp = new FileOpener(fi).open(false);
        xmlMetadata maskmeta = new xmlMetadata();
        File fmask = new File(maskpath + maskfilename);
        ImagePlus mask;
        //read or create masks
        if (!fmask.exists()) {
            System.out.println("This is the first stack");
            is_first = true;
            float[] data = new float[1024 * 1024];
            Arrays.fill(data, 20f);
            ImageProcessor maskip = new FloatProcessor(1024, 1024, data);
            mask = new ImagePlus("mask", maskip);
            maskmeta.ImgHeight = 1024;
            maskmeta.ImgWidth = 1024;
            maskmeta.bitdepth = 32;
            maskmeta.nImage = 1;
            maskmeta.create();
            maskmeta.save(maskpath + metaname);
            System.out.println(entropybackground);

        } else {
            System.out.println("Data and mask found");
            is_first = false;
            maskmeta.read(maskpath + metaname);
            FileInfo maskfi = new FileInfo();
            mask_width = maskmeta.ImgWidth;
            mask_height = maskmeta.ImgHeight;
            maskfi.width = mask_width;
            maskfi.height = mask_height;
            maskfi.nImages = 1;
            maskfi.fileType = fi.fileType;
            maskfi.intelByteOrder = true;
            maskfi.fileName = maskfilename;
            maskfi.directory = maskpath;
            mask = new FileOpener(maskfi).open(false);
            //test
        }
        File fconfig = new File(maskpath+"config.txt");
        entropybackground = maskmeta.entropybackground;
        angle_reso = maskmeta.ang_reso;
        ImageStack cropped = imp.getStack().crop(0, ystart, zstart, imp.getWidth(), yend - ystart, zend - zstart);
        ImagePlus croppedimp = new ImagePlus("cropped", cropped);

        run(croppedimp, mask);
        maskmeta.ImgHeight = mask.getHeight();
        maskmeta.ImgWidth = mask.getWidth();
        maskmeta.save(maskpath + metaname);


    }

    public static void main(String[] args) {
//        //filepath is the location of the image file along with meta.xml
        String filepath = args[0];
        //workspace is the location where all the mask/temp is located
        workspace = args[1];
        maskpath = workspace;
        idx = Integer.parseInt(args[2]);
        System.out.println("Starting analysis ");
        //progressive_processing(filepath);
        String filepathbase = "Z:\\Henry-SPIM\\smart_rotation\\04052018_corrected\\t0000\\conf";
        batch_processing(filepathbase,24);

    }
}
