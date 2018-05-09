package getsamplesize;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.plugin.CanvasResizer;
import ij.plugin.filter.RankFilters;
import ij.process.EllipseFitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static getsamplesize.analysis.compareresult.*;

public class analysis {
    private static int idx;
    private static String maskpath = "Z:\\Henry-SPIM\\11132017\\e2\\t0000\\";
    private static String maskfilename;
    private static int mask_width;
    private static int mask_height;
    private static double xypixelsize;
    private static double zpixelsize;
    private static int blk_size;
    private static double angle;
    private static float entropybackground;
    private static int angle_reso;
    private static int[] angle_count;
    private static float[] angle_avg;
    private static boolean is_first;
    private static String idxmaskfilename;
    private static int[][] out;
    private static void binarize(FloatProcessor ip, float max) {
        for (int i = 0; i < ip.getHeight(); i++) {
            for (int j = 0; j < ip.getWidth(); j++) {
                if (ip.getPixelValue(j, i) >= max) {

                    ip.setf(j, i, 0f);
                } else {
                    ip.setf(j, i, 255f);
                }
            }
        }
    }
    private static double[] get_ellipse(ImagePlus img) {


        FloatProcessor ip = (FloatProcessor) img.getProcessor();
//      Background entropy needs to be set by user (taking blank image and run analysis)
        binarize(ip, entropybackground);
        System.out.println("Binerized");
        RankFilters rf = new RankFilters();
        //remove outliers
        rf.rank(ip, 2 * blk_size, RankFilters.MEDIAN, RankFilters.BRIGHT_OUTLIERS, 50);
//        new ImageJ();
//        img.show();
        PointRoi roi = new PointRoi(ip.getHeight() / 2, ip.getWidth() / 2);
        int row_min;
        int row_max;
        boolean in_sample = false;
        for (int i = 0; i < ip.getHeight(); i++) {
            in_sample = false;
            row_min = ip.getWidth();
            row_max = 0;
            for (int j = 0; j < ip.getWidth(); j++) {
                if (ip.getPixelValue(j, i) == 255f) {
                    in_sample = true;
                    System.out.println("hit");
                    row_min = j < row_min ? j : row_min;
                    row_max = j > row_max ? j : row_max;
                }
            }
            if (in_sample) {
                roi.addPoint(row_min, i);
                roi.addPoint(row_max, i);
            }
        }
        System.out.println("ROI ADDED");
        img.setRoi(roi);
//        img.updateAndDraw();
//        img.show();


        PolygonRoi roipoly = new PolygonRoi(roi.getConvexHull(), Roi.POLYGON);
        ip.setRoi(roipoly);
        img.setRoi(roipoly);
        EllipseFitter ef = new EllipseFitter();
        ef.fit(ip, ip.getStatistics());

        double theta = ef.angle;
        double x = ef.xCenter;
        double y = ef.yCenter;
//        ImageProcessor iprescaled = ip.resize((int) Math.floor(ip.getWidth() * 16 * 0.65), (int) Math.floor(ip.getHeight() * 2));
//        ImagePlus imp_out = new ImagePlus("imp_out", iprescaled);

        double[] returnarray = {x, y, theta};
        for (int i = 0; i < 3; i++) {
            System.out.println(returnarray[i]);
        }

        return (returnarray);

    }
    private static void get_rainbow(String path) {
        System.out.println("Generating rainbox mask\n");
        maskfilename = "mask.raw";
        idxmaskfilename = "indexmask.raw";
        String metaname = "meta.xml";
        String filepath = path + "\\view0000\\";
        String dctname = compareresult.find_dct_img(filepath);
        //mask metafile updated to include sample location
        xmlMetadata meta = new xmlMetadata();
        meta.read(filepath + "meta.xml");
        //Information about the sample location from rawiamgeopener.java read
        int ystart = meta.samplestartx;
        int yend = meta.sampleendx;
        int zstart = meta.samplestartz;
        int zend = meta.sampleendz;
        angle = meta.anglepos;
        xypixelsize = meta.xypixelsize;
        zpixelsize = meta.zpixelsize;
        blk_size = meta.blk_size;
        /////////////////////////////////////////////////////////////////////////////////////////////
        //read dct image
        FileInfo fi = new FileInfo();
        fi.width = meta.ImgWidth / blk_size;
        fi.height = meta.ImgHeight / blk_size;
        fi.nImages = meta.nImage;
        fi.fileType = FileInfo.GRAY32_FLOAT;
        fi.intelByteOrder = true;
        fi.fileName = dctname;
        fi.directory = filepath;
        ImagePlus imp = new FileOpener(fi).open(false);
        ////////////////////////////////////////////////////////////////////////////////////////////
        //read the mask image
        xmlMetadata maskmeta = new xmlMetadata();
        File f = new File(maskpath + maskfilename);
        ImagePlus mask;
        ImagePlus idxmask;
        //read mask and read idxmask

        if (!f.exists()) {
            System.out.println("This is the first stack");
            is_first = true;
            float[] data = new float[1200 * 1200];
            float[] idxdata = new float[1200 * 1200];
            Arrays.fill(data, 20f);
            Arrays.fill(idxdata, 0f);
            ImageProcessor maskip = new FloatProcessor(1200, 1200, data);
            mask = new ImagePlus("mask", maskip);
            ImageProcessor idxmaskip = new FloatProcessor(1200, 1200, idxdata);
            idxmask = new ImagePlus("idxmask", idxmaskip);
            maskmeta.ImgHeight = 1200;
            maskmeta.ImgWidth = 1200;
            maskmeta.bitdepth = 32;
            maskmeta.nImage = 1;
            maskmeta.create();
            maskmeta.save(maskpath + metaname);
        } else {
            System.out.println("Data and mask found");
            is_first = false;
            maskmeta.read(maskpath + metaname);
            FileInfo maskfi = new FileInfo();
            mask_width = 1200;
            mask_height = 1200;
            maskfi.width = mask_width;
            maskfi.height = mask_height;
            maskfi.nImages = 1;
            maskfi.fileType = fi.fileType;
            maskfi.intelByteOrder = true;
            maskfi.fileName = maskfilename;
            maskfi.directory = maskpath;
            mask = new FileOpener(maskfi).open(false);
            FileInfo idxmaskfi = new FileInfo();
            idxmaskfi.width = 1200;
            idxmaskfi.height = 1200;
            idxmaskfi.nImages = 1;
            idxmaskfi.fileType = fi.fileType;
            idxmaskfi.intelByteOrder = true;
            idxmaskfi.fileName = idxmaskfilename;
            idxmaskfi.directory = maskpath;
            idxmask = new FileOpener(idxmaskfi).open(false);
        }
        entropybackground = maskmeta.entropybackground;
        angle_reso = maskmeta.ang_reso;
        //crop the image to get rid of background
        ImageStack cropped = imp.getStack().crop(0, ystart, zstart, imp.getWidth(), yend - ystart, zend - zstart);
        ImagePlus croppedimp = new ImagePlus("cropped", cropped);
        //update the mask to get new idx mask and mask
        rainbox_run(croppedimp, mask, idxmask);
    }
    private static void update_mask_rainbow(ImagePlus old_mask, ImagePlus new_mask, ImagePlus idxmask) {
        //get a maximum intensity projection between old_mask and new_mask to get a updated mask and store it in old_mask
        FloatProcessor old_data = (FloatProcessor) old_mask.getProcessor();
        FloatProcessor new_data = (FloatProcessor) new_mask.getProcessor();
        FloatProcessor idx_data = (FloatProcessor) idxmask.getProcessor();
        new_data.setInterpolationMethod(ImageProcessor.NONE);
        new_data = (FloatProcessor) new_data.resize((int) Math.floor(new_data.getWidth() * blk_size * xypixelsize), (int) Math.floor(new_data.getHeight() * zpixelsize), true);
        ImagePlus temp = new ImagePlus("temp", new_data);

        //padd mask or not]
        int maxwidth = 1200;
        int maxheight = 1200;
        CanvasResizer cr = new CanvasResizer();

        FloatProcessor oldmaskFP = (FloatProcessor) cr.expandImage(old_data, maxwidth, maxheight, (maxwidth - old_data.getWidth()) / 2, (maxheight - old_data.getHeight()) / 2);
        FloatProcessor newmaskFP = (FloatProcessor) cr.expandImage(new_data, maxwidth, maxheight, (maxwidth - new_data.getWidth()) / 2, (maxheight - new_data.getHeight()) / 2);
        newmaskFP.setBackgroundValue(entropybackground);
        FloatProcessor newmaskFPatOriginalSize = (FloatProcessor) newmaskFP.resize(newmaskFP.getWidth() / blk_size, newmaskFP.getHeight() / blk_size);
        newmaskFPatOriginalSize.rotate(angle);
        newmaskFP = (FloatProcessor) newmaskFPatOriginalSize.resize(newmaskFP.getWidth(), newmaskFP.getHeight());


        for (int i = 0; i < oldmaskFP.getHeight(); i++) {
            for (int j = 0; j < oldmaskFP.getWidth(); j++) {
                if (oldmaskFP.getPixelValue(j, i) > newmaskFP.getPixelValue(j, i)) {
                    oldmaskFP.setf(j, i, newmaskFP.getPixelValue(j, i));
                    if (oldmaskFP.getPixelValue(j, i) < entropybackground && oldmaskFP.getPixelValue(j, i) != 0f) {
//                        System.out.println(oldmaskFP.getPixelValue(j,i));
//                        if (idx == 0) {
//                            idx_data.setf(j, i, (float) idx + 1);
//                        } else {
//                            if (idx_data.getPixelValue(j, i) > 0) {
//                                idx_data.setf(j, i, (float) idx + 1);
//                            }
//                        }
//                    }
                        idx_data.setf(j, i, (float) idx + 1);
                    }
                }
                if (oldmaskFP.getPixelValue(j, i) <= 0.0f) {
                    oldmaskFP.setf(j, i, 20f);
                }
            }

        }
        compareresult.threshold(oldmaskFP, entropybackground);

        old_mask.setProcessor(oldmaskFP);

//        new ImageJ();
//        old_mask.show();
    }
    private static void manual_angle_calculation(String filepath, int[] indices) {
        ImagePlus mask_stack = IJ.openImage(filepath);
        ImagePlus old_mask = new ImagePlus();
        ImagePlus new_mask = new ImagePlus();
        int num_angles = indices.length;
        int max_idx = 0;
        int foregroundcount = 0;
        int numimgs = mask_stack.getNSlices();
        String savefilename = "mask";
        if (num_angles > numimgs) {
            System.out.println("Too many angles");
            return;
        } else {
            for (int i = 0; i < indices.length; i++) {

                if (i == 0) {
                    old_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(indices[i] + 1).duplicate());
                } else {
                    new_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(indices[i] + 1).duplicate());
                    update_mask_fixed(old_mask, new_mask);
                }
                savefilename += "_" + String.format("%02d", indices[i]);


            }
            foregroundcount = count_foreground(old_mask);
            IJ.saveAs(old_mask, "tif", maskpath + savefilename + ".tif");
            get_angular_result(old_mask);
            save_angular_result(savefilename + "_count.txt", savefilename + "_avg.txt");
            System.out.println(foregroundcount / 16 / 16);


        }
    }
    private static void update_mask_fixed(ImagePlus old_mask, ImagePlus new_mask) {
        FloatProcessor old_data = (FloatProcessor) old_mask.getProcessor();
        FloatProcessor new_data = (FloatProcessor) new_mask.getProcessor();
        for (int i = 0; i < old_mask.getHeight(); i++) {
            for (int j = 0; j < old_mask.getWidth(); j++) {
                if (new_data.getPixelValue(j, i) < old_data.getPixelValue(j, i)) {
                    old_data.setf(j, i, new_data.getPixelValue(j, i));
                }
            }
        }
    }
    private static void equal_angle_optimal(String filepath, int num_angles) {
        //Find the optimal amount of coverage with num_angles number of equally spaced angles
        //filepath is the location of the stack of padded masks
        is_first = false;
        ImagePlus mask_stack = IJ.openImage(filepath);
        ImagePlus old_mask = new ImagePlus();
        ImagePlus new_mask = new ImagePlus();
        int foregroundcount = 0;
        int numimgs = mask_stack.getNSlices();
        if (num_angles > numimgs) {
            System.out.println("Too many angles");
            return;
        } else {
            for (int i = 0; i < (numimgs); i++) {
                String savefilename = "mask";
                for (int j = 0; j < num_angles; j++) {
                    if (j == 0) {
                        old_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(i + 1).duplicate());
                    } else {
                        new_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor((i + numimgs / num_angles * j) % numimgs + 1).duplicate());
                        update_mask_fixed(old_mask, new_mask);
                    }
                    foregroundcount = count_foreground(old_mask) > foregroundcount ? count_foreground(old_mask) : foregroundcount;

                    savefilename += "_" + String.format("%02d", (i + numimgs / num_angles * j) % numimgs + 1);
                }


                IJ.saveAs(old_mask, "tif", maskpath + savefilename + ".tif");
                System.out.println(foregroundcount / 16 / 16);

            }
        }

    }
    private static void fix_one_angle_run_eq(String filepath, int initial_angle, int num_angles) {
        int[] array = new int[num_angles];
        for (int i = 0; i < array.length; i++) {
            array[i] = initial_angle + i * 24 / array.length;
        }
        manual_angle_calculation(filepath, array);
    }
    private static void rainbox_run(ImagePlus imp, ImagePlus mask, ImagePlus indexmask) {
        float[] projectedImageContainer = compareresult.sideprojection(imp);
        ImageProcessor ip = new FloatProcessor(imp.getHeight(), imp.getStackSize());
        ip.setPixels(projectedImageContainer);
        ImagePlus imp_out = new ImagePlus("output", ip);
        update_mask_rainbow(mask, imp_out, indexmask);
        IJ.saveAs(mask, "raw", maskpath + maskfilename);
        IJ.saveAs(indexmask, "raw", maskpath + "indexmask.raw");
        IJ.saveAs(indexmask, "raw", maskpath + "indexmask" + String.format("%02d", idx) + ".raw");
        System.out.println("Mask updated");

    }
    private static void fix_one_angle_run_flex(String filepath, int initial_angle, int num_angles) {
        ImagePlus mask_stack = IJ.openImage(filepath);
        ImagePlus old_mask = new ImagePlus();
        ImagePlus new_mask = new ImagePlus();
        int numimgs = 24;
        int foregroundcount = 0;
        int max_idx = 0;
        out = new int[binomi(numimgs - 1, num_angles - 1)][num_angles - 1];
        System.out.println(out.length);
        int arr[] = new int[numimgs - 1];
        int data[] = new int[num_angles - 1];
        for (int i = 0; i < numimgs; i++) {
            if (i < initial_angle) {
                arr[i] = i;
            } else if (i > initial_angle) {
                arr[i - 1] = i;
            } else continue;

        }
        compareresult.combinationUtil(arr, numimgs - 1, num_angles - 1, 0, data, 0);
        String savefilename = "mask";
        if (num_angles > numimgs) {
            System.out.println("Too many angles");
            return;
        } else {
            for (int i = 0; i < binomi(numimgs - 1, num_angles - 1); i++) {
                old_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(initial_angle + 1).duplicate());
                savefilename = "mask"+ "_" + String.format("%02d", initial_angle);
                for (int j = 0; j < num_angles - 1; j++) {

                    new_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(out[i][j]).duplicate());
                    update_mask_fixed(old_mask, new_mask);
                    savefilename += "_" + String.format("%02d", out[i][j]);
                    if (count_foreground(old_mask) > foregroundcount) {
                        System.out.println(out[i][j]);
                        foregroundcount = count_foreground(old_mask);
                        max_idx = i;
                        IJ.saveAs(old_mask, "tif", maskpath + savefilename + ".tif");
                    }

                }


                System.out.println(foregroundcount / 16 / 16);


            }
        }
    }
    private static void flexible_angle_optimal(String filepath, int num_angles) {
        //Find the optimal amount of coverage with num_angles number of equally spaced angles
        //filepath is the location of the stack of padded masks
        ImagePlus mask_stack = IJ.openImage(filepath);
        ImagePlus old_mask = new ImagePlus();
        ImagePlus new_mask = new ImagePlus();
        int max_idx = 0;
        int foregroundcount = 0;
        int numimgs = mask_stack.getNSlices();
        int data[] = new int[num_angles];
        int arr[] = new int[numimgs];
        for (int i = 0; i < numimgs; i++) {
            arr[i] = i + 1;
        }
        out = new int[binomi(numimgs, num_angles)][num_angles];
        combinationUtil(arr, numimgs, num_angles, 0, data, 0);
        if (num_angles > numimgs) {
            System.out.println("Too many angles");
            return;
        } else {
            for (int i = 0; i < binomi(numimgs, num_angles); i++) {
                String savefilename = "mask";
                for (int j = 0; j < num_angles; j++) {
                    if (j == 0) {
                        old_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(out[i][j]).duplicate());
                    } else {
                        new_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(out[i][j]).duplicate());
                        update_mask_fixed(old_mask, new_mask);
                    }
                    savefilename += "_" + String.format("%02d", out[i][j]);
                    if (count_foreground(old_mask) > foregroundcount) {
                        foregroundcount = count_foreground(old_mask);
                        max_idx = i;
                        IJ.saveAs(old_mask, "tif", maskpath + savefilename + ".tif");
                    }

                }


                System.out.println(foregroundcount / 16 / 16);


            }
        }
        for (int i = 0; i < out[0].length; i++) {
            System.out.println(out[max_idx][i]);
        }

    }

    public static class compareresult {
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

        static void threshold(FloatProcessor ip, float max) {
            for (int i = 0; i < ip.getHeight(); i++) {
                for (int j = 0; j < ip.getWidth(); j++) {
                    if (ip.getPixelValue(j, i) >= max) {

                        ip.setf(j, i, 20f);
                    }
                }
            }
        }

        static float[] sideprojection(ImagePlus imp) {

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
                //padd mask or not]
                int maxwidth = (int) Math.ceil(Math.max(old_data.getWidth(), new_data.getWidth()) / blk_size) * blk_size;
                int maxheight = (int) Math.ceil(Math.max(old_data.getHeight(), new_data.getHeight()) / blk_size) * blk_size;
                CanvasResizer cr = new CanvasResizer();
                FloatProcessor oldmaskFP = (FloatProcessor) cr.expandImage(old_data, maxwidth, maxheight, (maxwidth - old_data.getWidth()) / 2, (maxheight - old_data.getHeight()) / 2);
                FloatProcessor newmaskFP = (FloatProcessor) cr.expandImage(new_data, maxwidth, maxheight, (maxwidth - new_data.getWidth()) / 2, (maxheight - new_data.getHeight()) / 2);
                newmaskFP.setBackgroundValue(entropybackground);
                FloatProcessor newmaskFPatOriginalSize = (FloatProcessor) newmaskFP.resize(newmaskFP.getWidth() / blk_size, newmaskFP.getHeight() / blk_size);
                newmaskFPatOriginalSize.rotate(angle); //to avoid interpolation artefacts
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
                compareresult.threshold(oldmaskFP, entropybackground);

                old_mask.setProcessor(oldmaskFP);
            } else {
                int maxwidth = (int) Math.ceil(Math.max(new_data.getHeight(), new_data.getWidth()) / blk_size) * blk_size;
                int maxheight = (int) Math.ceil(Math.max(new_data.getHeight(), new_data.getWidth()) / blk_size) * blk_size;
                CanvasResizer cr = new CanvasResizer();
                FloatProcessor newmaskFP = (FloatProcessor) cr.expandImage(new_data, maxwidth, maxheight, (maxwidth - new_data.getWidth()) / 2, (maxheight - new_data.getHeight()) / 2);
                newmaskFP.setBackgroundValue(entropybackground);
                compareresult.threshold(newmaskFP, entropybackground);
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
            float[] projectedImageContainer = sideprojection(imp);
            ImageProcessor ip = new FloatProcessor(imp.getHeight(), imp.getStackSize());
            ip.setPixels(projectedImageContainer);
            ImagePlus imp_out = new ImagePlus("output", ip);
    //        ImagePlus imp_temp = imp_out;
    //        ImageProcessor iptemp = imp_temp.getProcessor();
    //        iptemp.setInterpolationMethod(ImageProcessor.NONE);
    //        iptemp = iptemp.resize((int)(iptemp.getWidth()*xypixelsize*blk_size),(int) (iptemp.getHeight()*zpixelsize));
    //        int maxwidth = 1200;
    //        int maxheight = 1200;
    //        CanvasResizer cr = new CanvasResizer();
    //        FloatProcessor oldmaskFP = (FloatProcessor) cr.expandImage(iptemp, maxwidth, maxheight, (maxwidth - iptemp.getWidth()) / 2, (maxheight - iptemp.getHeight()) / 2);
    //        imp_temp.setProcessor(oldmaskFP);
    //        IJ.saveAs(imp_temp,"raw",maskpath+curr_filename);
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
            File f = new File(maskpath + maskfilename);
            ImagePlus mask;
    //read or create masks
            if (!f.exists()) {
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
            }
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
    //        String filepath = args[0];
    //        //workspace is the location where all the mask/temp is located
    //        workspace = args[1];
    //        maskpath = workspace;
    //        idx = Integer.parseInt(args[2]);
    //        System.out.println("Starting analysis ");
    //        progressive_processing(filepath);
            String filepathbase = "Z:\\Henry-SPIM\\11132017\\e2\\t0000\\conf";
            batch_processing(filepathbase,24);


        }
    }
}
