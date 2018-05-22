package SmartRotationProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import mpicbg.ij.Mapping;

public class image_registration {
    public Mapping map;
    public int imgheight = 2000;
    public int imgwidth = 2000;
    public ImagePlus run(ImagePlus oldimg, ImagePlus newimg){
        IJ.run(oldimg,"Enhance Contrast","saturated = 0.35");
        IJ.run(newimg,"Enhance Contrast","saturated = 0.35");
        SIFT_align siftaligner = new SIFT_align();
        ImageProcessor ip = siftaligner.run(oldimg,newimg);
        map  = siftaligner.mapping;
        ImagePlus imp = new ImagePlus();
        imp.setProcessor(ip);
        return imp;
    }
    public ImageProcessor applymapping(ImagePlus img){
        ImageProcessor oldip = img.getProcessor();
        ImageProcessor alignedip = oldip.createProcessor(imgwidth,imgheight);
        alignedip.setMinAndMax(oldip.getMin(), oldip.getMax());
        map.mapInterpolated(oldip,alignedip);
        return alignedip;
    }
}
