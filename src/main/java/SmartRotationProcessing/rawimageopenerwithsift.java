package SmartRotationProcessing;
import ij.IJ;
import ij.ImageJ;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.FileOpener;
import java.io.File;
import java.util.stream.IntStream;
import ij.plugin.CanvasResizer;
import ij.plugin.filter.RankFilters;
import ij.process.*;
import ij.ImagePlus;


public class rawimageopenerwithsift {
    //version of rawimageopener that uses SIFT to register views
    private int rowStartIndex;
    private int rowEndIndex;
    private int colStartIndex;
    private int colEndIndex;
    private xmlMetadata meta;
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
        System.out.println(filepath);
        meta.read(filepath + "/meta.xml");
        System.out.println(meta.ImgHeight);
        String filename = datalocation.getName();
        //read raw image
        FileInfo fi = new FileInfo();
        meta.savetofileinfo(fi);
        fi.fileType = FileInfo.GRAY16_UNSIGNED;
        fi.fileName = filename;
        fi.directory = filepath;
        int background_value = meta.background;
        int blk_size = meta.blk_size;
        openAndCropImage(fi, background_value);
        rowStartIndex = Math.floorDiv(rowStartIndex, blk_size) * blk_size;
        rowEndIndex = (Math.floorDiv(rowEndIndex, blk_size) + 1) * blk_size;
        meta.samplestartz = rowStartIndex;
        meta.sampleendz = rowEndIndex;
        meta.samplestartx = colStartIndex / blk_size;
        meta.sampleendx = colEndIndex / blk_size;
        meta.save(filepath + "/meta.xml");
    }
}

