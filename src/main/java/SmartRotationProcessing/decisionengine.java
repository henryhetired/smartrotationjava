package SmartRotationProcessing;

import org.apache.commons.math3.fitting.WeightedObservedPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class decisionengine {
    private static int num_view_in;
    private static int num_angle_out;
    private static int ang_reso_eval;
    private static String workspace;
    public int[] angles;
    public void init(configwriter config,String workspacein){
        num_view_in = 360/config.ang_reso;
        num_angle_out = config.nAngles;
        ang_reso_eval = config.ang_reso_eval;
        angles = new int[num_angle_out];
        workspace = workspacein;
    }
    public void get_strategy(int timepoint,int gen_figure){
        try {
            String workingpath = System.getProperty("user.dir");
            Process p = Runtime.getRuntime().exec(workingpath + "/python/evaluationstep_final.py "+workspace+" "+Integer.toString(timepoint)+" "+Integer.toString(gen_figure));
            BufferedReader stdInput = new BufferedReader((new InputStreamReader(p.getInputStream())));
            String s;
            String[] output = new String[num_angle_out];
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                output = s.split(",");
            }
            for (int i=0;i<output.length;i++){
                angles[i] = Integer.parseInt(output[i]);
            }
            System.exit(0);
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}