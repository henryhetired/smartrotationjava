package getsamplesize;
import ij.IJ;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.FileOpener;
import java.io.File;
import java.util.ArrayList;
import java.util.stream.IntStream;
import ij.plugin.CanvasResizer;
import ij.plugin.filter.RankFilters;
import ij.process.*;
import ij.ImagePlus;


public class rawimageopener {
    private int rowStartIndex;
    private int rowEndIndex;
    private int colStartIndex;
    private int colEndIndex;
    private xmlMetadata meta;
    private void openAndCropImage(FileInfo fi, int bkgnd_value) {
        //open image based on fileinfo fi
        ImagePlus imp = new FileOpener(fi).open(false);
        ImageProcessor ip = new ShortProcessor(imp.getHeight(), imp.getStackSize());
        //project the image from the side (assuming the sample is not aligned with the camera direction
        short[] projectedImageContainer = sideprojection_raw(imp, 0, imp.getWidth(), imp.getWidth(), imp.getStackSize(), imp.getHeight());
        ip.setPixels(projectedImageContainer);
        ImagePlus imp_out = new ImagePlus("output", ip);
        get_bound(imp_out.getProcessor(), bkgnd_value);
        Roi sample_only = new Roi(colStartIndex, rowStartIndex, (colEndIndex - colStartIndex), (rowEndIndex - rowStartIndex));
        ip.setRoi(sample_only);
        ImageProcessor croppedip = ip.crop();
        CanvasResizer cr = new CanvasResizer();
        ImageProcessor rescaledip = croppedip.resize((int) Math.floor(croppedip.getWidth() * meta.xypixelsize), (int)(croppedip.getHeight() * meta.zpixelsize));
        ImageProcessor paaedip = cr.expandImage(rescaledip, 1200, 1200, (1200 - rescaledip.getWidth()) / 2, (1200 - rescaledip.getHeight()) / 2);
        ImagePlus cropped = new ImagePlus("cropped", paaedip);
        IJ.saveAs(cropped, "tif", fi.directory+"cropped.tif");

    }

    private void get_bound(ImageProcessor ip, int bkgnd_value) {
//          Background value needs to be set by user (taking blank image and run analysis)
        ImageProcessor ipdup = ip.duplicate();
        threshold(ipdup, bkgnd_value);
        RankFilters rf = new RankFilters();
        //remove outliers
        rf.rank(ipdup, 20, RankFilters.MEDIAN, RankFilters.BRIGHT_OUTLIERS, 50);
        rowStartIndex = ipdup.getHeight();
        rowEndIndex = 0;
        colStartIndex = ipdup.getWidth();
        colEndIndex = 0;
        for (int i = 0; i < ipdup.getHeight(); i++) {
            for (int j = 0; j < ipdup.getWidth(); j++) {
                if (ipdup.getPixel(j, i) == 255) {
                    if (j < colStartIndex) {
                        colStartIndex = j;
                    }
                    if (j > colEndIndex) {
                        colEndIndex = j;
                    }
                    if (i < rowStartIndex) {
                        rowStartIndex = i;
                    }
                    if (i > rowEndIndex) {
                        rowEndIndex = i;
                    }
                }

            }
        }
    }

    private void threshold(ImageProcessor input, int min) {
        for (int i = 0; i < input.getHeight(); i++) {
            for (int j = 0; j < input.getWidth(); j++) {
                if (input.getPixel(j, i) > min) {
                    input.set(j, i, 255);
                } else {
                    input.set(j, i, 0);
                }
            }

        }
    }

    private short[] sideprojection_raw(ImagePlus imp, int startx, int endx, int width, int stacksize, int height) {

        //////////////////////////////////////////
        //getpixels // Editing pixel at x,y position new_pixels[y * width + x] = ...;
        //new implementation with direct access to data within the container
        //also parallelize the stack operation since they are independent
        short[] imagecontainer = new short[stacksize * height];
        IntStream.range(0, stacksize).parallel().forEach(ziter -> {
            ImageProcessor ip = imp.getStack().getProcessor(ziter + 1);
            for (int yiter = 0; yiter < height; yiter++) {

                short maxvalue = 0;
                for (int xiter = startx; xiter < endx; xiter++) {
                    maxvalue = (short) Math.max(maxvalue, ip.getPixel(xiter, yiter));
                }
                imagecontainer[ziter * height + yiter] = maxvalue;
            }
        });
        return imagecontainer;
    }


    public void run(String path) {
        meta = new xmlMetadata();
        File datalocation = new File(path);
        String filepath = datalocation.getParent().replace("\\", "\\\\");
        meta.read(filepath + "\\meta.xml");
        String filename = datalocation.getName();
        //read raw image
        FileInfo fi = new FileInfo();
        fi.width = meta.ImgWidth;
        fi.height = meta.ImgHeight;
        fi.nImages = meta.nImage;
        fi.fileType = FileInfo.GRAY16_UNSIGNED;
        fi.intelByteOrder = true;
        fi.fileName = filename;
        fi.directory = filepath;
        fi.gapBetweenImages = meta.gapbetweenimages;
        int background_value = meta.background;
        int blk_size = meta.blk_size;
        openAndCropImage(fi, background_value);
        rowStartIndex = Math.floorDiv(rowStartIndex, blk_size) * blk_size;
        rowEndIndex = (Math.floorDiv(rowEndIndex, blk_size) + 1) * blk_size;
        meta.samplestartz = rowStartIndex;
        meta.sampleendz = rowEndIndex;
        meta.samplestartx = colStartIndex / blk_size;
        meta.sampleendx = colEndIndex / blk_size;
        meta.save(filepath + "\\meta.xml");
    }
}
