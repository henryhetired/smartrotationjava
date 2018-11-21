package SmartRotationProcessing;

import org.apache.commons.math3.fitting.WeightedObservedPoint;

import java.util.ArrayList;

public class decisionengine {
    private int numofangleforevaluation;
    private int numofangleforimaging;
    public int[] current_angles;
    private vonmisesfitter fitter;
    private boolean initialzied = false;
    private boolean model_exist = false;
    private double[][] parameters; //von mises parameters for each angular slice [slice_index][0:amp,1:cen,2:kappa]
    private double[][] histograms; //angular foreground count for each view [view idx][slice index]
    public void init(configwriter config){
        fitter = new vonmisesfitter();
        numofangleforevaluation = 360/config.ang_reso;
        numofangleforimaging = config.nAngles;
        current_angles = new int[numofangleforimaging];
        for (int i=0;i<numofangleforimaging;i++){
            current_angles[i] = 360/numofangleforimaging*i;
        }
        parameters = new double[numofangleforevaluation][3];
        histograms = new double[numofangleforevaluation][numofangleforevaluation];
        initialzied = true;
    }
    public void get_parameters(){
        //from the histogram, estimate the parameters for each angular slice
        if(initialzied){
            for (int i=0;i<parameters.length;i++){
                ArrayList<WeightedObservedPoint> points = new ArrayList<WeightedObservedPoint>();
                for (int j=0;j<histograms.length;j++){
                    points.add(new WeightedObservedPoint(1.0,j*360/numofangleforevaluation,histograms[j][i]));
                }
                double[] result = fitter.fit(points);
                parameters[i][0] = result[0];
                parameters[i][1] = result[1];
                parameters[i][2] = result[2];
            }
        }
    }
    public void update_histogram(double[] histogramin,int angular_idx){
        if (initialzied && histogramin.length==histograms[0].length){
            if (angular_idx>=histograms.length){
                System.out.println("Incorrect index for histogram to update");
            }
            else{
                histograms[angular_idx]=histogramin;
            }
        }
    }
    public void get_strategy(){
        if (initialzied){

        }
    }

}
