//package SmartRotationProcessing;
//
//import ij.IJ;
//import ij.ImagePlus;
//import ij.ImageStack;
//import ij.gui.PointRoi;
//import ij.gui.PolygonRoi;
//import ij.gui.Roi;
//import ij.io.FileInfo;
//import ij.io.FileOpener;
//import ij.plugin.CanvasResizer;
//import ij.plugin.filter.RankFilters;
//import ij.process.EllipseFitter;
//import ij.process.FloatProcessor;
//import ij.process.ImageProcessor;
//import java.io.File;
//import java.util.Arrays;
//
//import static SmartRotationProcessing.compareresult.count_foreground;
//import static SmartRotationProcessing.compareresult.get_angular_result;
//import static SmartRotationProcessing.compareresult.save_angular_result;
//
//public class analysis {
//    private static int idx;
//    private static String maskpath = "Z:\\Henry-SPIM\\11132017\\e2\\t0000\\";
//    private static String maskfilename;
//    private static int mask_width;
//    private static int mask_height;
//    private static double xypixelsize;
//    private static double zpixelsize;
//    private static int blk_size;
//    private static double angle;
//    private static float entropybackground;
//    private static int angle_reso;
//    private static int[] angle_count;
//    private static float[] angle_avg;
//    private static boolean is_first;
//    private static String idxmaskfilename;
//    private static int[][] out;
//    private static void binarize(FloatProcessor ip, float max) {
//        for (int i = 0; i < ip.getHeight(); i++) {
//            for (int j = 0; j < ip.getWidth(); j++) {
//                if (ip.getPixelValue(j, i) >= max) {
//
//                    ip.setf(j, i, 0f);
//                } else {
//                    ip.setf(j, i, 255f);
//                }
//            }
//        }
//    }
//    private static double[] get_ellipse(ImagePlus img) {
//
//
//        FloatProcessor ip = (FloatProcessor) img.getProcessor();
////      Background entropy needs to be set by user (taking blank image and run analysis)
//        binarize(ip, entropybackground);
//        System.out.println("Binerized");
//        RankFilters rf = new RankFilters();
//        //remove outliers
//        rf.rank(ip, 2 * blk_size, RankFilters.MEDIAN, RankFilters.BRIGHT_OUTLIERS, 50);
////        new ImageJ();
////        img.show();
//        PointRoi roi = new PointRoi(ip.getHeight() / 2, ip.getWidth() / 2);
//        int row_min;
//        int row_max;
//        boolean in_sample = false;
//        for (int i = 0; i < ip.getHeight(); i++) {
//            in_sample = false;
//            row_min = ip.getWidth();
//            row_max = 0;
//            for (int j = 0; j < ip.getWidth(); j++) {
//                if (ip.getPixelValue(j, i) == 255f) {
//                    in_sample = true;
//                    System.out.println("hit");
//                    row_min = j < row_min ? j : row_min;
//                    row_max = j > row_max ? j : row_max;
//                }
//            }
//            if (in_sample) {
//                roi.addPoint(row_min, i);
//                roi.addPoint(row_max, i);
//            }
//        }
//        System.out.println("ROI ADDED");
//        img.setRoi(roi);
////        img.updateAndDraw();
////        img.show();
//
//
//        PolygonRoi roipoly = new PolygonRoi(roi.getConvexHull(), Roi.POLYGON);
//        ip.setRoi(roipoly);
//        img.setRoi(roipoly);
//        EllipseFitter ef = new EllipseFitter();
//        ef.fit(ip, ip.getStatistics());
//
//        double theta = ef.angle;
//        double x = ef.xCenter;
//        double y = ef.yCenter;
////        ImageProcessor iprescaled = ip.resize((int) Math.floor(ip.getWidth() * 16 * 0.65), (int) Math.floor(ip.getHeight() * 2));
////        ImagePlus imp_out = new ImagePlus("imp_out", iprescaled);
//
//        double[] returnarray = {x, y, theta};
//        for (int i = 0; i < 3; i++) {
//            System.out.println(returnarray[i]);
//        }
//
//        return (returnarray);
//
//    }
////    private static void get_rainbow(String path) {
//    System.out.println("Generating rainbox mask\n");
//        maskfilename = "mask.raw";
//        idxmaskfilename = "indexmask.raw";
//        String metaname = "meta.xml";
//        String filepath = path + "\\view0000\\";
//        String dctname = compareresult.find_dct_img(filepath);
//        //mask metafile updated to include sample location
//        xmlMetadata meta = new xmlMetadata();
//        meta.read(filepath + "meta.xml");
//        //Information about the sample location from rawiamgeopener.java read
//        int ystart = meta.samplestartx;
//        int yend = meta.sampleendx;
//        int zstart = meta.samplestartz;
//        int zend = meta.sampleendz;
//        angle = meta.anglepos;
//        xypixelsize = meta.xypixelsize;
//        zpixelsize = meta.zpixelsize;
//        blk_size = meta.blk_size;
//        /////////////////////////////////////////////////////////////////////////////////////////////
//        //read dct image
//        FileInfo fi = new FileInfo();
//        fi.width = meta.ImgWidth / blk_size;
//        fi.height = meta.ImgHeight / blk_size;
//        fi.nImages = meta.nImage;
//        fi.fileType = FileInfo.GRAY32_FLOAT;
//        fi.intelByteOrder = true;
//        fi.fileName = dctname;
//        fi.directory = filepath;
//        ImagePlus imp = new FileOpener(fi).open(false);
//        ////////////////////////////////////////////////////////////////////////////////////////////
//        //read the mask image
//        xmlMetadata maskmeta = new xmlMetadata();
//        File f = new File(maskpath + maskfilename);
//        ImagePlus mask;
//        ImagePlus idxmask;
//        //read mask and read idxmask
//
//        if (!f.exists()) {
//            System.out.println("This is the first stack");
//            is_first = true;
//            float[] data = new float[1200 * 1200];
//            float[] idxdata = new float[1200 * 1200];
//            Arrays.fill(data, 20f);
//            Arrays.fill(idxdata, 0f);
//            ImageProcessor maskip = new FloatProcessor(1200, 1200, data);
//            mask = new ImagePlus("mask", maskip);
//            ImageProcessor idxmaskip = new FloatProcessor(1200, 1200, idxdata);
//            idxmask = new ImagePlus("idxmask", idxmaskip);
//            maskmeta.ImgHeight = 1200;
//            maskmeta.ImgWidth = 1200;
//            maskmeta.bitdepth = 32;
//            maskmeta.nImage = 1;
//            maskmeta.create();
//            maskmeta.save(maskpath + metaname);
//        } else {
//            System.out.println("Data and mask found");
//            is_first = false;
//            maskmeta.read(maskpath + metaname);
//            FileInfo maskfi = new FileInfo();
//            mask_width = 1200;
//            mask_height = 1200;
//            maskfi.width = mask_width;
//            maskfi.height = mask_height;
//            maskfi.nImages = 1;
//            maskfi.fileType = fi.fileType;
//            maskfi.intelByteOrder = true;
//            maskfi.fileName = maskfilename;
//            maskfi.directory = maskpath;
//            mask = new FileOpener(maskfi).open(false);
//            FileInfo idxmaskfi = new FileInfo();
//            idxmaskfi.width = 1200;
//            idxmaskfi.height = 1200;
//            idxmaskfi.nImages = 1;
//            idxmaskfi.fileType = fi.fileType;
//            idxmaskfi.intelByteOrder = true;
//            idxmaskfi.fileName = idxmaskfilename;
//            idxmaskfi.directory = maskpath;
//            idxmask = new FileOpener(idxmaskfi).open(false);
//        }
//        entropybackground = maskmeta.entropybackground;
//        angle_reso = maskmeta.ang_reso;
//        //crop the image to get rid of background
//        ImageStack cropped = imp.getStack().crop(0, ystart, zstart, imp.getWidth(), yend - ystart, zend - zstart);
//        ImagePlus croppedimp = new ImagePlus("cropped", cropped);
//        //update the mask to get new idx mask and mask
//        rainbox_run(croppedimp, mask, idxmask);
//    }
//    private static void update_mask_rainbow(ImagePlus old_mask, ImagePlus new_mask, ImagePlus idxmask) {
//        //get a maximum intensity projection between old_mask and new_mask to get a updated mask and store it in old_mask
//        FloatProcessor old_data = (FloatProcessor) old_mask.getProcessor();
//        FloatProcessor new_data = (FloatProcessor) new_mask.getProcessor();
//        FloatProcessor idx_data = (FloatProcessor) idxmask.getProcessor();
//        new_data.setInterpolationMethod(ImageProcessor.NONE);
//        new_data = (FloatProcessor) new_data.resize((int) Math.floor(new_data.getWidth() * blk_size * xypixelsize), (int) Math.floor(new_data.getHeight() * zpixelsize), true);
//        ImagePlus temp = new ImagePlus("temp", new_data);
//
//        //padd mask or not]
//        int maxwidth = 1200;
//        int maxheight = 1200;
//        CanvasResizer cr = new CanvasResizer();
//
//        FloatProcessor oldmaskFP = (FloatProcessor) cr.expandImage(old_data, maxwidth, maxheight, (maxwidth - old_data.getWidth()) / 2, (maxheight - old_data.getHeight()) / 2);
//        FloatProcessor newmaskFP = (FloatProcessor) cr.expandImage(new_data, maxwidth, maxheight, (maxwidth - new_data.getWidth()) / 2, (maxheight - new_data.getHeight()) / 2);
//        newmaskFP.setBackgroundValue(entropybackground);
//        FloatProcessor newmaskFPatOriginalSize = (FloatProcessor) newmaskFP.resize(newmaskFP.getWidth() / blk_size, newmaskFP.getHeight() / blk_size);
//        newmaskFPatOriginalSize.rotate(angle);
//        newmaskFP = (FloatProcessor) newmaskFPatOriginalSize.resize(newmaskFP.getWidth(), newmaskFP.getHeight());
//
//
//        for (int i = 0; i < oldmaskFP.getHeight(); i++) {
//            for (int j = 0; j < oldmaskFP.getWidth(); j++) {
//                if (oldmaskFP.getPixelValue(j, i) > newmaskFP.getPixelValue(j, i)) {
//                    oldmaskFP.setf(j, i, newmaskFP.getPixelValue(j, i));
//                    if (oldmaskFP.getPixelValue(j, i) < entropybackground && oldmaskFP.getPixelValue(j, i) != 0f) {
////                        System.out.println(oldmaskFP.getPixelValue(j,i));
////                        if (idx == 0) {
////                            idx_data.setf(j, i, (float) idx + 1);
////                        } else {
////                            if (idx_data.getPixelValue(j, i) > 0) {
////                                idx_data.setf(j, i, (float) idx + 1);
////                            }
////                        }
////                    }
//                        idx_data.setf(j, i, (float) idx + 1);
//                    }
//                }
//                if (oldmaskFP.getPixelValue(j, i) <= 0.0f) {
//                    oldmaskFP.setf(j, i, 20f);
//                }
//            }
//
//        }
//        compareresult.threshold_entropy(oldmaskFP, entropybackground);
//
//        old_mask.setProcessor(oldmaskFP);
//
////        new ImageJ();
////        old_mask.show();
//    }
//    private static void manual_angle_calculation(String filepath, int[] indices) {
//        ImagePlus mask_stack = IJ.openImage(filepath);
//        ImagePlus old_mask = new ImagePlus();
//        ImagePlus new_mask = new ImagePlus();
//        int num_angles = indices.length;
//        int max_idx = 0;
//        int foregroundcount = 0;
//        int numimgs = mask_stack.getNSlices();
//        String savefilename = "mask";
//        if (num_angles > numimgs) {
//            System.out.println("Too many angles");
//            return;
//        } else {
//            for (int i = 0; i < indices.length; i++) {
//
//                if (i == 0) {
//                    old_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(indices[i] + 1).duplicate());
//                } else {
//                    new_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(indices[i] + 1).duplicate());
//                    update_mask_fixed(old_mask, new_mask);
//                }
//                savefilename += "_" + String.format("%02d", indices[i]);
//
//
//            }
//            foregroundcount = count_foreground(old_mask);
//            IJ.saveAs(old_mask, "tif", maskpath + savefilename + ".tif");
//            get_angular_result(old_mask);
//            save_angular_result(savefilename + "_count.txt", savefilename + "_avg.txt");
//            System.out.println(foregroundcount / 16 / 16);
//
//
//        }
//    }
//    private static void update_mask_fixed(ImagePlus old_mask, ImagePlus new_mask) {
//        FloatProcessor old_data = (FloatProcessor) old_mask.getProcessor();
//        FloatProcessor new_data = (FloatProcessor) new_mask.getProcessor();
//        for (int i = 0; i < old_mask.getHeight(); i++) {
//            for (int j = 0; j < old_mask.getWidth(); j++) {
//                if (new_data.getPixelValue(j, i) < old_data.getPixelValue(j, i)) {
//                    old_data.setf(j, i, new_data.getPixelValue(j, i));
//                }
//            }
//        }
//    }
//    private static void equal_angle_optimal(String filepath, int num_angles) {
//        //Find the optimal amount of coverage with num_angles number of equally spaced angles
//        //filepath is the location of the stack of padded masks
//        is_first = false;
//        ImagePlus mask_stack = IJ.openImage(filepath);
//        ImagePlus old_mask = new ImagePlus();
//        ImagePlus new_mask = new ImagePlus();
//        int foregroundcount = 0;
//        int numimgs = mask_stack.getNSlices();
//        if (num_angles > numimgs) {
//            System.out.println("Too many angles");
//            return;
//        } else {
//            for (int i = 0; i < (numimgs); i++) {
//                String savefilename = "mask";
//                for (int j = 0; j < num_angles; j++) {
//                    if (j == 0) {
//                        old_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(i + 1).duplicate());
//                    } else {
//                        new_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor((i + numimgs / num_angles * j) % numimgs + 1).duplicate());
//                        update_mask_fixed(old_mask, new_mask);
//                    }
//                    foregroundcount = count_foreground(old_mask) > foregroundcount ? count_foreground(old_mask) : foregroundcount;
//
//                    savefilename += "_" + String.format("%02d", (i + numimgs / num_angles * j) % numimgs + 1);
//                }
//
//
//                IJ.saveAs(old_mask, "tif", maskpath + savefilename + ".tif");
//                System.out.println(foregroundcount / 16 / 16);
//
//            }
//        }
//
//    }
//    private static void fix_one_angle_run_eq(String filepath, int initial_angle, int num_angles) {
//        int[] array = new int[num_angles];
//        for (int i = 0; i < array.length; i++) {
//            array[i] = initial_angle + i * 24 / array.length;
//        }
//        manual_angle_calculation(filepath, array);
//    }
//    private static void rainbox_run(ImagePlus imp, ImagePlus mask, ImagePlus indexmask) {
//        float[] projectedImageContainer = compareresult.sideprojection_entropy(imp);
//        ImageProcessor ip = new FloatProcessor(imp.getHeight(), imp.getStackSize());
//        ip.setPixels(projectedImageContainer);
//        ImagePlus imp_out = new ImagePlus("output", ip);
//        update_mask_rainbow(mask, imp_out, indexmask);
//        IJ.saveAs(mask, "raw", maskpath + maskfilename);
//        IJ.saveAs(indexmask, "raw", maskpath + "indexmask.raw");
//        IJ.saveAs(indexmask, "raw", maskpath + "indexmask" + String.format("%02d", idx) + ".raw");
//        System.out.println("Mask updated");
//
//    }
//    private static void fix_one_angle_run_flex(String filepath, int initial_angle, int num_angles) {
//        ImagePlus mask_stack = IJ.openImage(filepath);
//        ImagePlus old_mask = new ImagePlus();
//        ImagePlus new_mask = new ImagePlus();
//        int numimgs = 24;
//        int foregroundcount = 0;
//        int max_idx = 0;
//        out = new int[compareresult.binomi(numimgs - 1, num_angles - 1)][num_angles - 1];
//        System.out.println(out.length);
//        int arr[] = new int[numimgs - 1];
//        int data[] = new int[num_angles - 1];
//        for (int i = 0; i < numimgs; i++) {
//            if (i < initial_angle) {
//                arr[i] = i;
//            } else if (i > initial_angle) {
//                arr[i - 1] = i;
//            } else continue;
//
//        }
//        compareresult.combinationUtil(arr, numimgs - 1, num_angles - 1, 0, data, 0);
//        String savefilename = "mask";
//        if (num_angles > numimgs) {
//            System.out.println("Too many angles");
//            return;
//        } else {
//            for (int i = 0; i < compareresult.binomi(numimgs - 1, num_angles - 1); i++) {
//                old_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(initial_angle + 1).duplicate());
//                savefilename = "mask"+ "_" + String.format("%02d", initial_angle);
//                for (int j = 0; j < num_angles - 1; j++) {
//
//                    new_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(out[i][j]).duplicate());
//                    update_mask_fixed(old_mask, new_mask);
//                    savefilename += "_" + String.format("%02d", out[i][j]);
//                    if (count_foreground(old_mask) > foregroundcount) {
//                        System.out.println(out[i][j]);
//                        foregroundcount = count_foreground(old_mask);
//                        max_idx = i;
//                        IJ.saveAs(old_mask, "tif", maskpath + savefilename + ".tif");
//                    }
//
//                }
//
//
//                System.out.println(foregroundcount / 16 / 16);
//
//
//            }
//        }
//    }
//    private static void flexible_angle_optimal(String filepath, int num_angles) {
//        //Find the optimal amount of coverage with num_angles number of equally spaced angles
//        //filepath is the location of the stack of padded masks
//        ImagePlus mask_stack = IJ.openImage(filepath);
//        ImagePlus old_mask = new ImagePlus();
//        ImagePlus new_mask = new ImagePlus();
//        int max_idx = 0;
//        int foregroundcount = 0;
//        int numimgs = mask_stack.getNSlices();
//        int data[] = new int[num_angles];
//        int arr[] = new int[numimgs];
//        for (int i = 0; i < numimgs; i++) {
//            arr[i] = i + 1;
//        }
//        out = new int[compareresult.binomi(numimgs, num_angles)][num_angles];
//        compareresult.combinationUtil(arr, numimgs, num_angles, 0, data, 0);
//        if (num_angles > numimgs) {
//            System.out.println("Too many angles");
//            return;
//        } else {
//            for (int i = 0; i < compareresult.binomi(numimgs, num_angles); i++) {
//                String savefilename = "mask";
//                for (int j = 0; j < num_angles; j++) {
//                    if (j == 0) {
//                        old_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(out[i][j]).duplicate());
//                    } else {
//                        new_mask.setProcessor((FloatProcessor) mask_stack.getStack().getProcessor(out[i][j]).duplicate());
//                        update_mask_fixed(old_mask, new_mask);
//                    }
//                    savefilename += "_" + String.format("%02d", out[i][j]);
//                    if (count_foreground(old_mask) > foregroundcount) {
//                        foregroundcount = count_foreground(old_mask);
//                        max_idx = i;
//                        IJ.saveAs(old_mask, "tif", maskpath + savefilename + ".tif");
//                    }
//
//                }
//
//
//                System.out.println(foregroundcount / 16 / 16);
//
//
//            }
//        }
//        for (int i = 0; i < out[0].length; i++) {
//            System.out.println(out[max_idx][i]);
//        }
//
//    }
//
//
//}
