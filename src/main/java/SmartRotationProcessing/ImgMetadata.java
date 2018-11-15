package SmartRotationProcessing;

import java.io.*;

public class ImgMetadata {
    //Class to read and create ImgMetadata file
    public int ImgWidth = 2048;
    public int ImgHeight = 2048;
    public int nImage = 500;
    public double xypixelsizeum = 0.65d;
    public double zpixelsizeum = 2d;
    public double xpos = 0.0d;
    public double ypos = 0.0d;
    public double startzpos = 0.0d;
    public double endzpos = 0.0d;
    public double anglepos = 0;
    public int bitdepth = 16;
    public int gapbetweenimages = 4;
    public String filepattern = "t%04d_conf%04d_view0000.tif";
    public void read(String filename) throws IOException{
        //image meta file reader to get the metadata
        try(BufferedReader br = new BufferedReader(new FileReader(filename))){
            String line;
            while((line = br.readLine())!=null){
                if (line.contains("=")){
                    if (line.contains("ImgWidth")){
                        ImgWidth = Integer.parseInt(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("ImgHeight")){
                        ImgHeight = Integer.parseInt(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("nImage")){
                        nImage = Integer.parseInt(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("xypixelsizeum")){
                        xypixelsizeum = Double.parseDouble(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("zpixelsizeum")){
                        zpixelsizeum = Double.parseDouble(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("xpos")){
                        xpos = Double.parseDouble(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("ypos")){
                        ypos = Double.parseDouble(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("startz")){
                        startzpos = Double.parseDouble(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("endz")){
                        endzpos = Double.parseDouble(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("angle")){
                        anglepos = Double.parseDouble(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("bitdepth")){
                        bitdepth = Integer.parseInt(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("gapbetweenimages")){
                        gapbetweenimages = Integer.parseInt(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("file pattern string")){

                        filepattern = line.substring(line.indexOf("=")+1);
                    }
                }
            }
            br.close();

        }
    }
}
