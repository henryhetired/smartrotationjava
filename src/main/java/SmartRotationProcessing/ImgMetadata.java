package SmartRotationProcessing;

import java.io.*;

public class ImgMetadata {
    //Class to read and create ImgMetadata file
    public static int ImgWidth = 2048;
    public static int ImgHeight = 2048;
    public static int nImage = 500;
    public static double xypixelsizeum = 0.65d;
    public static double zpixelsizeum = 2d;
    public static double xpos = 0.0d;
    public static double ypos = 0.0d;
    public static double startzpos = 0.0d;
    public static double endzpos = 0.0d;
    public static double anglepos = 0;
    public static int bitdepth = 16;
    public static int gapbetweenimages = 4;
    public static String filepattern = "t%04d_conf%04d_view0000.tif";
    public static void read(String filename) throws IOException{
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
                    if (line.contains("nImage")){
                        nImage = Integer.parseInt(line.substring(line.indexOf("=")+1));
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
