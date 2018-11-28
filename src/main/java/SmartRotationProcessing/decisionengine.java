package SmartRotationProcessing;

import org.apache.commons.math3.fitting.WeightedObservedPoint;

import java.util.ArrayList;

public class decisionengine {
    private int numofangleforevaluation;
    private int numofangleforimaging;
    public ArrayList<Integer> current_angles;
    private vonmisesfitter fitter;
    private vonmises function;
    private boolean initialzied = false;
    private double[][] parameters; //von mises parameters for each angular slice [slice_index][0:amp,1:cen,2:kappa]
    private Integer[][] histograms; //angular foreground count for each view [view idx][slice index]

    public void init(configwriter config) {
        fitter = new vonmisesfitter();
        function = new vonmises();
        numofangleforevaluation = 360 / config.ang_reso;
        numofangleforimaging = config.nAngles;
        current_angles = new ArrayList<Integer>(numofangleforimaging);
        for (int i = 0; i < numofangleforimaging; i++) {
            current_angles.add(0);
            current_angles.set(i, i*360 / numofangleforimaging);
        }
        parameters = new double[numofangleforevaluation][3];
        histograms = new Integer[numofangleforevaluation][numofangleforevaluation];
        initialzied = true;
    }

    public void get_parameters() {
        //from the histogram, estimate the parameters for each angular slice, primarily to make data smoother
        if (initialzied) {
            for (int i = 0; i < parameters.length; i++) {
                ArrayList<WeightedObservedPoint> points = new ArrayList<WeightedObservedPoint>();
                for (int j = 0; j < histograms.length; j++) {
                    points.add(new WeightedObservedPoint(1.0, j * 360 / numofangleforevaluation, histograms[j][i]));
                }
                double[] result = fitter.fit(points);
                parameters[i][0] = result[0];
                parameters[i][1] = result[1];
                parameters[i][2] = result[2];
            }
        }
    }

    public void update_histogram(Integer[] histogramin, int angular_idx) {
        //update the histogram at index angular_idx
        if (initialzied && histogramin.length == histograms[0].length) {
            if (angular_idx >= histograms.length) {
                System.out.println("Incorrect index for histogram to update");
            } else {
                histograms[angular_idx] = histogramin;
            }
        }
    }

    public ArrayList<ArrayList<Integer>> combine(int n, int k) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

        if (n <= 0 || n < k)
            return result;

        ArrayList<Integer> item = new ArrayList<Integer>();
        dfs(n, k, 0, item, result); // because it need to begin from 1

        return result;
    }

    private void dfs(int n, int k, int start, ArrayList<Integer> item,
                     ArrayList<ArrayList<Integer>> res) {
        if (item.size() == k) {
            res.add(new ArrayList<Integer>(item));
            return;
        }

        for (int i = start; i <= n; i++) {
            item.add(i);
            dfs(n, k, i + 1, item, res);
            item.remove(item.size() - 1);
        }
    }

    public double[] generate_distribution(double[] parameters) {
        //generate coverage from a parameter set for von mises distribution
        if (initialzied) {
            double x;
            double[] result = new double[numofangleforevaluation];
            for (int i = 0; i < numofangleforevaluation; i++) {
                x = 360.0 / numofangleforevaluation * i;
                result[i] = function.value(x, parameters);
            }
            return result;
        } else {
            return (null);
        }

    }

    public double[] get_coverage(ArrayList<Integer> angles_idx) {
        if (initialzied) {
            double[] return_histogram = new double[numofangleforevaluation];
            for (int i = 0; i < angles_idx.size(); i++) {
                for (int j = 0; j < numofangleforevaluation; j++) {
                    double angle_slice = j * 360.0 / numofangleforevaluation;
                    double estimate = function.value(angle_slice, parameters[i]);
                    if (estimate > return_histogram[j]) {
                        return_histogram[j] = estimate;
                    }
                }
            }
            return (return_histogram);
        } else {
            return (null);
        }
    }

    public double get_coverage_percentage(double[] histogram) {
        if (initialzied) {
            double sum = 0.0;
            for (int i = 0; i < histogram.length; i++) {
                sum += histogram[i] / parameters[i][0];
            }
            return (sum);
        } else {
            return (0);
        }


    }

    public void get_strategy() {
        //from the current parameters, calculate the angles that gives the maximum coverage
        if (initialzied) {
            ArrayList<ArrayList<Integer>> indices_list = combine(numofangleforevaluation, numofangleforimaging);
            double current_result = 0.0;
            int current_winner = 0;
            for (int i = 0; i < indices_list.size(); i++) {
                double[] current_coverage = get_coverage(indices_list.get(i));
                double temp = get_coverage_percentage(current_coverage);
                if (current_result < temp) {
                    current_winner = i;
                }
            }
            current_angles = indices_list.get(current_winner);
        }
    }

}
