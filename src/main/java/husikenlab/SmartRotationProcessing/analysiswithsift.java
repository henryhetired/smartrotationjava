package husikenlab.SmartRotationProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class analysiswithsift {
    public void generate_rainbow_plot(String workspace, int num_images, int gap) {
        //Function to generate a rainbow plot of ALL angles taken showing the optimal angle for each subregion
        ImagePlus mask = IJ.createImage("mask", "32-bit black", 2000, 2000, 1);
        ImageProcessor maskprocessor = mask.getProcessor();
        String firstfilename = workspace + String.format("maskdct%02d.tif", 0);
        ImagePlus first_image = IJ.openImage(firstfilename);
        ImageProcessor first_image_processor = first_image.getProcessor();
        for (int i = 0; i < num_images; i += gap) {
            String filename = workspace + String.format("maskdct%02d.tif", i);
            ImagePlus current_img = IJ.openImage(filename);
            ImageProcessor current_imp = current_img.getProcessor();
            for (int j = 0; j < current_img.getHeight(); j++) {
                for (int k = 0; k < current_img.getWidth(); k++) {
                    if (current_imp.getPixelValue(k, j) < first_image_processor.getPixelValue(k, j)) {
                        maskprocessor.setf(k, j, i);
                    }
                }
            }
            mask.setProcessor(maskprocessor);
            first_image_processor = current_imp;
            IJ.saveAs(mask, "tiff", workspace + String.format("maskrainbow%02d.tif", i));
        }
    }

    public ImagePlus run_comparison(String workspace, String[] anglesname) {

        ImageStack stack = new ImageStack(2000, 2000);

        for (String x : anglesname) {
            rawimageopenerwithsift ir = new rawimageopenerwithsift();
            ImagePlus temp = new ImagePlus();
            temp = ir.process_raw_image(new ImagePlus(workspace + x + "_raw.tif"), 150);
            ImagePlus temp2 = new ImagePlus();
            temp2 = ir.process_dct_image(new ImagePlus(workspace + x + "_dct.tif"), temp);
            IJ.saveAs(temp2, "tif", workspace + x + "_dct_masked.tif");
            ImageProcessor ip = temp2.getProcessor();
            stack.addSlice(ip);
        }
        ImagePlus imp = new ImagePlus();
        imp.setStack(stack);
        IJ.saveAs(imp, "tiff", workspace + "dct_resized.tif");
        return (imp);

    }

    public void generate_comparison(String workspace) {
        //Angle selection based on python analysis, used for making figure 5.
        String[] twoanglesname = {"0,12", "7,20", "8,21"};
        String[] threeanglesname = {"0,8,16", "4,9,20", "8,18,23"};
        String[] fouranglesname = {"0,6,12,18", "4,9,18,22"};
        String workspacetemp = workspace + Integer.toString(2) + "angles/";

        run_comparison(workspacetemp, twoanglesname);
        workspacetemp = workspace + Integer.toString(3) + "angles/";
        run_comparison(workspacetemp, threeanglesname);
        workspacetemp = workspace + Integer.toString(4) + "angles/";
        run_comparison(workspacetemp, fouranglesname);


    }
}
