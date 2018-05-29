package SmartRotationProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.io.File;

public class analysiswithsift {
    public void generate_rainbow_plot(String workspace,int num_images){
        //Function to generate a rainbow plot of ALL angles taken showing the optimal angle for each subregion
        ImagePlus mask = IJ.createImage("mask","32-bit black",2000,2000,1);
        ImageProcessor maskprocessor = mask.getProcessor();
        String firstfilename = workspace + String.format("maskdct%02d.tif",0);
        ImagePlus first_image = IJ.openImage(firstfilename);
        ImageProcessor first_image_processor = first_image.getProcessor();
        for (int i=0;i<num_images;i++){
            String filename = workspace + String.format("maskdct%02d.tif",i);
            ImagePlus current_img = IJ.openImage(filename);
            ImageProcessor current_imp = current_img.getProcessor();
            for (int j=0;j<current_img.getHeight();j++){
                for (int k=0;k<current_img.getWidth();k++){
                    if (current_imp.getPixelValue(k,j)<first_image_processor.getPixelValue(k,j)){
                        maskprocessor.setf(k,j,i);
                    }
                }
            }
            mask.setProcessor(maskprocessor);
            first_image_processor = current_imp;
            IJ.saveAs(mask,"tiff",workspace+String.format("maskrainbow%02d.tif",i));
        }
    }
}
