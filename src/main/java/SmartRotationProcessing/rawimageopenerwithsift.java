package SmartRotationProcessing;

import ij.IJ;
import ij.ImageJ;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.FileOpener;

import java.io.File;
import java.util.stream.IntStream;

import ij.plugin.CanvasResizer;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.RankFilters;
import ij.process.*;
import ij.ImagePlus;
import org.apache.commons.io.FilenameUtils;

public class rawimageopenerwithsift {
    //version of rawimageopener that uses SIFT to register views
    public xmlMetadata meta;
    public static ImagePlus rawImage;
    public static ImagePlus dctImage;
    public ImagePlus imageMask;
    public String filepath;
    public String workspace;
    public String filenamebase;
    public boolean initialized=false;
    public void init(String filepathin,String workpathin,ImagePlus raw,ImagePlus dct){
        filepath = filepathin;
        workspace = workpathin;
        rawImage = raw;
        dctImage = dct;
        initialized = true;
        filenamebase = FilenameUtils.getBaseName(filepathin);
        filepath = FilenameUtils.getFullPath(filepathin);
        meta = new xmlMetadata();
        meta.read(filepath + filenamebase + ".xml");
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
    private short[] sideprojection_raw(ImagePlus imp) {

        //////////////////////////////////////////
        //getpixels // Editing pixel at x,y position new_pixels[y * width + x] = ...;
        //new implementation with direct access to data within the container
        //also parallelize the stack operation since they are independent
        short[] imagecontainer = new short[imp.getStackSize() * imp.getHeight()];
        IntStream.range(0, imp.getStackSize()).parallel().forEach(ziter -> {
            ImageProcessor ip = imp.getStack().getProcessor(ziter + 1);
            for (int yiter = 0; yiter < imp.getHeight(); yiter++) {

                short maxvalue = 0;
                for (int xiter = 0; xiter < imp.getWidth(); xiter++) {
                    maxvalue = (short) Math.max(maxvalue, ip.getPixel(xiter, yiter));
                }
                imagecontainer[ziter * imp.getHeight() + yiter] = maxvalue;
            }
        });
        return imagecontainer;
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

    public ImagePlus process_raw_image(ImagePlus rawimage,int background){
        //remove outlier for the raw image
        ImageProcessor ip = rawimage.getProcessor().duplicate();
//        RankFilters rf = new RankFilters();
//        //remove outliers
//        rf.rank(ip, 150, RankFilters.MEDIAN, RankFilters.BRIGHT_OUTLIERS, 50);

        ip.threshold(background);
        ip.max(1);
        ImagePlus imgmask = new ImagePlus();
        imgmask.setProcessor(ip);

        return(imgmask);
    }
    public ImagePlus process_dct_image(ImagePlus dctimage,ImagePlus imgmask){
        ImageCalculator ic = new ImageCalculator();
        return (ic.run("multiply create",dctimage,imgmask));
    }

    public void project_raw_image() {
        if (initialized) {
            short[] pixelfromtop = sideprojection_raw(rawImage);
            ShortProcessor rawtopimage = new ShortProcessor(rawImage.getWidth(), rawImage.getNSlices());
            rawtopimage.setPixels(pixelfromtop);
            rawtopimage.setInterpolationMethod(ImageProcessor.BILINEAR);
            ShortProcessor rawtopimageresized = (ShortProcessor) rawtopimage.resize((int) Math.floor(rawtopimage.getWidth() * meta.xypixelsize), (int) (rawtopimage.getHeight() * meta.zpixelsize));
            CanvasResizer cr = new CanvasResizer();
            ImageProcessor expandedoutput = cr.expandImage(rawtopimageresized, 2000, 2000, (2000 - rawtopimageresized.getWidth()) / 2, (2000 - rawtopimageresized.getHeight()) / 2);
            expandedoutput.rotate(-meta.anglepos);
            rawImage.close();
            rawImage = new ImagePlus();
            rawImage.setProcessor(expandedoutput);
            imageMask = process_raw_image(rawImage, 150);
            IJ.saveAs(rawImage, "tif", workspace + filenamebase + ".tif");
        }
        else{
            System.out.println("No file read");
            return;
        }

    }

    public void project_dct_image() {
        if (initialized) {
            float[] pixelfromtop = sideprojection_entropy(dctImage);
            FloatProcessor floattopimage = new FloatProcessor(dctImage.getWidth(), dctImage.getNSlices());
            floattopimage.setPixels(pixelfromtop);
            floattopimage.setInterpolationMethod(ImageProcessor.BILINEAR);
            FloatProcessor floattopimageresized = (FloatProcessor) floattopimage.resize((int) Math.floor(floattopimage.getWidth() * meta.xypixelsize * meta.blk_size), (int) (floattopimage.getHeight() * meta.zpixelsize));
            CanvasResizer cr = new CanvasResizer();
            ImageProcessor expandedoutput = cr.expandImage(floattopimageresized, 2000, 2000, (2000 - floattopimageresized.getWidth()) / 2, (2000 - floattopimageresized.getHeight()) / 2);
            expandedoutput.setBackgroundValue(meta.entropybackground);
            expandedoutput.setInterpolationMethod(0);
            expandedoutput.rotate(-meta.anglepos);
            dctImage.close();
            dctImage = new ImagePlus();
            dctImage.setProcessor(expandedoutput);
            IJ.saveAs(process_dct_image(dctImage, imageMask), "tif", workspace + filenamebase + "_dct.tif");
        }
        else{
            System.out.println("No file read");
            return;
        }

    }

    public void run() {
        //Process image to generate a top down projection on both the raw image and the dct image
        project_raw_image();
        project_dct_image();
    }
}

