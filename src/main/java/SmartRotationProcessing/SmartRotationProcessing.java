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
    private static Integer[] angle_count;
    public static ImagePlus rawImg;
    public static ImagePlus dctImg;
    private static int idx;
    private static int current_timepoint;
    private static boolean usesift = true;
    private static boolean useregistration = true;
    private static float downsamplefactor = 1;
    private static dctCUDAencoding cuda;
    public boolean initialized = false;
    public configwriter config;
    private static boolean evaluated;
    private static int reference_tp;
    private String filenamebase;
    public decisionengine de;

    static void threshold_entropy(FloatProcessor ip, float max) {
        //function to threshold the entropy
        for (int i = 0; i < ip.getHeight(); i++) {
            for (int j = 0; j < ip.getWidth(); j++) {
                if (ip.getPixelValue(j, i) >= max) {

                    ip.setf(j, i, 20f);
                }
                if (ip.getPixelValue(j, i) == 0f) {
                    ip.setf(j, i, 20f);
                }
            }
        }
    }

    private static void get_angular_result(ImagePlus img) {
        //the function that calculates the angular foreground count
        angle_count = new Integer[360 / angle_reso];
        for (int i=0;i<angle_count.length;i++){
            angle_count[i] = 0;
        }
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
        Integer[] temp = new Integer[angle_count.length];
        for (int i=0;i<angle_count.length;i++){
            temp[i] = angle_count[angle_count.length-i-1];
        }
        angle_count = temp;
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

    private static String get_last_mask(String workspace) {
        File dir = new File(workspace);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastmodifiedmaskFile = files[0];
        for (int i = 0; i < files.length; i++) {
            if (lastmodifiedmaskFile.lastModified() <= files[i].lastModified() && files[i].getName().contains("dct")) {
                lastmodifiedmaskFile = files[i];
            }
        }
        return lastmodifiedmaskFile.getName();
    }

    private static String get_last_raw(String workspace) {
        File dir = new File(workspace);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        File lastmodifiedmaskFile = files[0];
        for (int i = 0; i < files.length; i++) {
            if (idx > 0) {
                if (files[i].lastModified() >= lastmodifiedmaskFile.lastModified() && !files[i].getName().contains("dct.tif")&& !files[i].getName().contains("txt")) {
                    lastmodifiedmaskFile = files[i];
                }
            } else {
                if (!files[i].getName().contains("dct.tif") && !files[i].getName().contains("txt")) {
                    lastmodifiedmaskFile = files[i];
                }
            }

        }
        return lastmodifiedmaskFile.getName();
    }

    public void evaluation_run(String filepath) {
        //run steps for all the evaluation steps
        cudaEncode();
        rawimageopenerwithsift rows = new rawimageopenerwithsift();
        rows.init(filepath, workspace, rawImg, dctImg, config);
        rows.run();
        String latestmask = filenamebase+"_dct.tif"; //latest reference of the DCT mask
        String latestraw = filenamebase+".tif";//latest reference MIP
        System.out.println("latestest image is " + latestraw);
        System.out.println("latest mask is " + latestmask);
        if (idx == 0) {
            System.out.println("This is the first stack");
            ImagePlus og = rows.rawImage;
            ImagePlus new_dct_transformed = rows.dctImage;
            if (useregistration) {
                image_registration ir = new image_registration();
                ir.use_SIFT = false;

                og = ir.run(og, og);

                new_dct_transformed.setProcessor(ir.applymapping(new_dct_transformed));
            }
            IJ.saveAs(og, "tif", workspace + latestraw);
            get_angular_result(new_dct_transformed);
            de.update_histogram(angle_count, idx);
            save_angular_result(String.format("angularcount%04d_%04d.txt", current_timepoint, idx));
            IJ.saveAs(new_dct_transformed, "tif", workspace + latestmask);
            return;
        } else {
            ImagePlus new_mask = rows.dctImage;
            ImagePlus old_raw = new ImagePlus(workspace+String.format(FilenameUtils.getBaseName(config.filepattern)+".tif", current_timepoint, idx - 1));
            ImagePlus new_raw = rows.rawImage;
            ImagePlus new_dct_transformed = new ImagePlus();
            if (useregistration) {
                image_registration ir = new image_registration();
                ir.downsamplingfactor = downsamplefactor;
                ir.use_SIFT = usesift;
                new_raw = ir.run(old_raw, new_raw);
                new_dct_transformed.setProcessor(ir.applymapping(new_mask));
            } else {
                new_dct_transformed.setProcessor(new_mask.getProcessor());
            }
            IJ.saveAs(new_raw, "tif", workspace + latestraw);
            get_angular_result(new_dct_transformed);
            de.update_histogram(angle_count, idx);
            save_angular_result(String.format("angularcount%04d_%04d.txt", current_timepoint, idx));
            IJ.saveAs(new_dct_transformed, "tif", workspace + latestmask);
        }
        return;
    }

    public void evaluation_step(String filepath, int timepoint, int num_angles, int gap) {

        //workspace is the location where all the mask/temp is located
        //Evaluate all the images at timepoint with num_angles number of angles
        System.out.println("Starting analysis:");
        current_timepoint = timepoint;
        for (int i = 0; i < num_angles; i += gap) {
            idx = i;
            String filename = filepath + String.format(config.filepattern, timepoint, idx);
            open_image(filename); //open either tif or raw files
            evaluation_run(filename);
            //IJ.saveAs(dctImg,"tiff",workspace+String.format("t0000_conf%04d_view0000_c00_dct.tif",i));
        }
        reference_tp = current_timepoint;
    }

    public void update_step(String filepath, int angle_idx, int timepoint) {
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
            ImagePlus old_raw = new ImagePlus(workspace + String.format(filenamebase+".tif", reference_tp, idx));
            ImagePlus new_raw = rows.rawImage;
            ImagePlus new_dct_transformed = new ImagePlus();
            if (useregistration) {
                image_registration ir = new image_registration();
                ir.downsamplingfactor = downsamplefactor;
                ir.use_SIFT = usesift;
                new_raw = ir.run(old_raw, new_raw);
                new_dct_transformed.setProcessor(ir.applymapping(new_mask));
            } else {
                new_dct_transformed.setProcessor(new_mask.getProcessor());
            }
            IJ.saveAs(new_raw, "tif", workspace+String.format(filenamebase+"tif", timepoint, angle_idx));
            get_angular_result(new_dct_transformed);
            de.update_histogram(angle_count, angle_idx);
            save_angular_result(String.format("angularcount%04d_%04d.txt", current_timepoint, idx));
            IJ.saveAs(new_dct_transformed, "tif", workspace + FilenameUtils.getBaseName(filename) + "_dct.tif");
            System.out.println("Evaluation completed");
        } else {
            System.out.println("No evaluation step has been performed, terminating");
            return;
        }
    }

    private void cudaEncode() {
        if (initialized) {
            long start_time = System.currentTimeMillis();
            if (rawImg.getStackSize() > 500) {
                downsamplefactor = 1.6f;
            } else {
                downsamplefactor = 1f;
            }
            cuda.stack = rawImg;
            try {
                cuda.dct_encoding_run();
            } catch (IOException e) {
                e.printStackTrace();
            }
            long tp2 = System.currentTimeMillis() - start_time;
            System.out.println("Encoding time is " + tp2 + " ms");
            dctImg = cuda.entropyimg;
            evaluated = true;
        }
    }

    private void open_image(String filename) {
        if (filename.endsWith("tif")) {
            rawImg = new ImagePlus(filename);
        } else if (filename.endsWith("raw")) {
            String filenamein = FilenameUtils.removeExtension(filename);
            filenamebase = FilenameUtils.getBaseName(filename);
            ImgMetadata meta = new ImgMetadata();
            try {
                meta.read(filenamein + ".txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileInfo fi = new FileInfo();
            fi.fileType = FileInfo.GRAY16_UNSIGNED;
            fi.fileName = filename;
            fi.width = meta.ImgWidth;
            fi.height = meta.ImgHeight;
            fi.nImages = meta.nImage;
            fi.gapBetweenImages = meta.gapbetweenimages;
            fi.intelByteOrder=true;
            rawImg = new FileOpener(fi).open(false);
        }
    }

    public void init(String workspacein) {
        //initialize cuda device and prep
        workspace = workspacein;
        cuda = new dctCUDAencoding();
        cuda.init_cuda();
        //default registration parameters
        useregistration = true;
        usesift = true;
        String configname = workspace;
        config = new configwriter();
        try {
            config.read(configname);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cuda.blk_size = config.blk_size;
        angle_reso = config.ang_reso;
        idx = 0;
        current_timepoint = 0;
        reference_tp = 0;
        de = new decisionengine();
        de.init(config);
        initialized = true;
    }
}
