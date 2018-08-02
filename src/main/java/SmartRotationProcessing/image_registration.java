package SmartRotationProcessing;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import mpicbg.ij.InverseTransformMapping;
import mpicbg.ij.Mapping;
import mpicbg.ij.TransformMapping;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.CoordinateTransform;
import mpicbg.models.InverseCoordinateTransform;
import mpicbg.models.TranslationModel2D;
import fiji.selection.*;
import java.awt.*;

public class image_registration {
    public Mapping map;
    public int imgheight = 2000;
    public int imgwidth = 2000;
    public boolean use_SIFT = false;
    public float downsamplingfactor = 1.6f;
    public ImagePlus run(ImagePlus oldimg, ImagePlus newimg){
        if (use_SIFT) {
            IJ.run(oldimg, "Enhance Contrast", "saturated = 0.35");
            IJ.run(newimg, "Enhance Contrast", "saturated = 0.35");
            SIFT_align siftaligner = new SIFT_align();
            siftaligner.downsamplefactor=downsamplingfactor;
            ImageProcessor ip = siftaligner.run(oldimg, newimg);
            map = siftaligner.mapping;
            TranslationModel2D model = (TranslationModel2D) siftaligner.currentModel;
            ImagePlus imp = new ImagePlus();
            imp.setProcessor(ip);
            return imp;
        }
        else{
            ImagePlus temp = newimg.duplicate();
            ImageProcessor ip = temp.getProcessor();
            //threshold image, plance in the center
            IJ.run(temp, "Enhance Contrast", "saturated = 0.35");
            IJ.setAutoThreshold(newimg,"Default dark");
            IJ.run(temp,"Convert to Mask","");
            IJ.run(temp,"Fill Holes","");
            Select_Bounding_Box selection = new Select_Bounding_Box();
            selection.setup("autoselect",temp);
            selection.run(temp.getProcessor());
            Roi boundingbox =temp.getRoi();
            Rectangle rec = boundingbox.getBounds();
            TranslationModel2D model = new TranslationModel2D();
//            System.out.println(rec.x);
//            System.out.println(rec.y);
//            System.out.println(rec.width);
//            System.out.println(rec.height);
            model.set((1000-rec.x-rec.width/2),(1000-rec.y-rec.height/2));
            map = new InverseTransformMapping<AbstractAffineModel2D<?>>(model);
            ImageProcessor alignedimg = ip.createProcessor(newimg.getWidth(), newimg.getHeight());
            alignedimg.setMinAndMax(ip.getMin(), ip.getMax());
            map.mapInterpolated(ip,alignedimg);
            newimg.setProcessor(alignedimg);
            return newimg;


        }


    }
    public ImageProcessor applymapping(ImagePlus img){
        ImageProcessor oldip = img.getProcessor();
        ImageProcessor alignedip = oldip.createProcessor(imgwidth,imgheight);
        alignedip.setMinAndMax(oldip.getMin(), oldip.getMax());
        map.mapInterpolated(oldip,alignedip);
        return alignedip;
    }

}
