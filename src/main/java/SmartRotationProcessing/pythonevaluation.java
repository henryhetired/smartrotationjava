package SmartRotationProcessing;

import java.io.*;
public class pythonevaluation {
    public String scriptlocation;
    public String pythonlocation;
    public double[] amp;
    public double[] centroid;
    public double[] kappa;
    public void pycalltest(String foldername,int num_angles,int angular_resolution){
        String s = null;
        String result = "";
        try {
            Process p = Runtime.getRuntime().exec(pythonlocation+" " + scriptlocation + " " + foldername + " " + Integer.toString(num_angles) + " " + Integer.toString(angular_resolution));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));
            while((s=stdInput.readLine())!=null){
//                System.out.println(s);
                result +=s;
            }
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            String[] trimmedresult = result.replace("[","").split("[]]");
            amp = new double[360/angular_resolution];
            centroid = new double[360/angular_resolution];
            kappa = new double[360/angular_resolution];
            String[] tempamp = trimmedresult[0].replaceFirst("^ ", "").split("[ ]+");
            String[] tempcen = trimmedresult[1].replaceFirst("^ ", "").split("[ ]+");
            String[] tempkap = trimmedresult[2].replaceFirst("^ ", "").split("[ ]+");
            for (int i=0;i<tempamp.length;i++){
                amp[i] = Double.parseDouble(tempamp[i]);
                centroid[i] = Double.parseDouble(tempcen[i]);
                kappa[i] = Double.parseDouble(tempkap[i]);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
}
