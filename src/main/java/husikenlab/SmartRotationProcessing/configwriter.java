package husikenlab.SmartRotationProcessing;


import java.io.*;

public class configwriter {
    //this is the smart rotation configuration file
    public int blk_size = 16;
    public int backgroundintensity = 700;
    public float entropybackground = 7.2f;
    public int ang_reso_eval = 10;
    public int ang_reso = 15;
    public int nAngles = 4;
    public String filepattern = "t%04d_conf%04d.tif";
    public void create(String filepath) throws IOException {
        //create a config file with the default parameters at filepath
        File fout = new File(filepath + "config.txt");
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write("This is the configuration file for the Smart Rotation Workflow, Please follow the instructions from the Readme.txt");
        bw.newLine();
        bw.write(new String(new char[30]).replace("\0", "-"));
        bw.newLine();
        bw.write("***");
        bw.write("Image Parameters");
        bw.write("***");
        bw.newLine();
        bw.write("file pattern string=" + filepattern);
        bw.newLine();
        bw.write(new String(new char[30]).replace("\0", "-"));
        bw.newLine();
        bw.write("***");
        bw.write("Workflow Parameters");
        bw.write("***");
        bw.newLine();
        bw.write("blk_size=" + Integer.toString(blk_size));
        bw.newLine();
        bw.write("backgroundintensity=" + Integer.toString(backgroundintensity));
        bw.newLine();
        bw.write("entropybackground=" + Float.toString(entropybackground));
        bw.newLine();
        bw.write("angular resolution=" + Integer.toString(ang_reso));
        bw.newLine();
        bw.write("evaluation resolution=" + Integer.toString(ang_reso_eval));
        bw.newLine();
        bw.write("nAngles=" + Integer.toString(nAngles));
        bw.newLine();
        bw.write(new String(new char[30]).replace("\0", "-"));
        bw.close();
        System.out.println("Config File successfully created: " + filepath + "config.txt");
    }

    public void read(String filepath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath + "config.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("=")) {
                    if (line.contains("blk_size")) {
                        blk_size = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                    }
                    if (line.contains("backgroundintensity")) {
                        backgroundintensity = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                    }
                    if (line.contains("entropybackground")) {
                        entropybackground = Float.parseFloat(line.substring(line.indexOf("=") + 1));
                    }
                    if (line.contains("angular resolution")) {
                        ang_reso = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                    }
                    if (line.contains("evaluation resolution")) {
                        ang_reso_eval = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                    }
                    if (line.contains("file pattern string")) {

                        filepattern = line.substring(line.indexOf("=") + 1);
                    }
                    if (line.contains("nAngle")) {

                        nAngles = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                    }
                }
            }

        }
    }

}
