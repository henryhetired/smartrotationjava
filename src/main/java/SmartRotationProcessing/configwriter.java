package SmartRotationProcessing;

import org.w3c.dom.Document;

import java.io.*;

public class configwriter {
    //this is the xml file class for the stack meta file
    public int ImgWidth = 2048;
    public int ImgHeight = 2048;
    public int nImage = 500;
    public double xypixelsize = 0.65d;
    public double zpixelsize = 2d;
    public int bitdepth = 16;
    public int blk_size = 16;
    public int gapbetweenimages = 4;
    public int backgroundintensity = 700;
    public float entropybackground = 7.2f;
    public int ang_reso=10;
    private static Document doc;
    public void create (String filepath) throws IOException {
        //create a config file with the default parameters at filepath
        File fout = new File(filepath+"config.txt");
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write("This is the configuration file for the Smart Rotation Workflow, Please follow the instructions from the Readme.txt");
        bw.newLine();
        bw.write(new String(new char[30]).replace("\0","*"));
        bw.newLine();
        bw.write("Image Parameters");
        bw.newLine();
        bw.write("ImgWidth="+Integer.toString(ImgWidth));
        bw.newLine();
        bw.write("ImgHeight="+Integer.toString(ImgHeight));
        bw.newLine();
        bw.write("nImage="+Integer.toString(nImage));
        bw.newLine();
        bw.write("xypixelsize="+Double.toString(xypixelsize));
        bw.newLine();
        bw.write("zpixelsize="+Double.toString(zpixelsize));
        bw.newLine();
        bw.write("bitdepth="+Integer.toString(bitdepth));
        bw.newLine();
        bw.write("gapbetweenimages="+Integer.toString(gapbetweenimages));
        bw.newLine();
        bw.write(new String(new char[30]).replace("\0","*"));
        bw.newLine();
        bw.write("Workflow Parameters");
        bw.newLine();
        bw.write("blk_size="+Integer.toString(blk_size));
        bw.newLine();
        bw.write("backgroundintensity="+Integer.toString(backgroundintensity));
        bw.newLine();
        bw.write("entropybackground="+Float.toString(entropybackground));
        bw.newLine();
        bw.write("angular resolution="+Integer.toString(ang_reso));
        bw.close();
        System.out.println("Config File successfully created: "+filepath+"config.txt");
    }
    public void read (String filepath) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(filepath+"config.txt"))){
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
                    if (line.contains("xypixelsize")){
                        xypixelsize = Double.parseDouble(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("zpixelsize")){
                        zpixelsize = Double.parseDouble(line.substring(line.indexOf("=")+1));
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
                    if (line.contains("blk_size")){
                        blk_size = Integer.parseInt(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("backgroundintensity")){
                        backgroundintensity = Integer.parseInt(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("entropybackground")){
                        entropybackground = Float.parseFloat(line.substring(line.indexOf("=")+1));
                    }
                    if (line.contains("angular resolution")){
                        ang_reso = Integer.parseInt(line.substring(line.indexOf("=")+1));
                    }
                }
            }
            br.close();

        }
    }

}
