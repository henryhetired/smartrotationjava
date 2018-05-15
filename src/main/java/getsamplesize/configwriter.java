package getsamplesize;

import java.io.File;

public class configwriter {
    //all the parameters of the configuration
    public int ImgWidth = 2048;
    public int ImgHeight = 2048;
    public int nImage = 500;
    public double xypixelsize = 0.65d;
    public double zpixelsize = 2d;

    public double xpos = 0.0d;
    public double ypos = 0.0d;
    public double startzpos = 0.0d;
    public double endzpos = 0.0d;
    public double anglepos = 0;
    public int samplestartx = 0;
    public int sampleendx = 512;
    public int samplestartz = 0;
    public int sampleendz = 500;
    public int bitdepth = 16;
    public int blk_size = 16;
    public int gapbetweenimages = 4;
    public int background = 700;
    public float entropybackground = 7.2f;
    private void read(String filepath){
        File configfile = new File(filepath);
        if (configfile.getName() !="config.txt"){
            System.out.println("Config file naming convention error");
            return;
        }
        if (!configfile.exists()){
            System.out.println("Config file not found");
            return;
        }
    }
}
