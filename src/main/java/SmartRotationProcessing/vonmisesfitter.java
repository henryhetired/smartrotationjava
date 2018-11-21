package SmartRotationProcessing;

import java.util.*;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.linear.DiagonalMatrix;

import static cern.jet.math.Bessel.i0;

class vonmises implements ParametricUnivariateFunction {
    public double value(double x, double... parameters) {
        /*parameters[0]=A
          parameters[1]=cen in radian
          parameters[2]=kappa
          x in degrees
          */
        double top = (parameters[0] / Math.PI * 2 * i0(parameters[2]));
        double bottom = Math.exp(parameters[2] * Math.cos(x / 360.0 * 2.0 * Math.PI - parameters[1]));
        return (top * bottom);
    }

    public double[] gradient(double t, double... parameters) {
        final double a = parameters[0];
        final double b = parameters[1];
        final double c = parameters[2];

        // Jacobian Matrix Edit

        // Using Derivative Structures...
        // constructor takes 4 arguments - the number of parameters in your
        // equation to be differentiated (3 in this case), the order of
        // differentiation for the DerivativeStructure, the index of the
        // parameter represented by the DS, and the value of the parameter itself
        DerivativeStructure aDev = new DerivativeStructure(3, 1, 0, a);
        DerivativeStructure bDev = new DerivativeStructure(3, 1, 1, b);
        DerivativeStructure cDev = new DerivativeStructure(3, 1, 2, c);

        // define the equation to be differentiated using another DerivativeStructure
        DerivativeStructure y = aDev.multiply(DerivativeStructure.pow(t, bDev))
                .multiply(cDev.negate().multiply(t).exp());

        // then return the partial derivatives required
        // notice the format, 3 arguments for the method since 3 parameters were
        // specified first order derivative of the first parameter, then the second,
        // then the third
        return new double[]{
                y.getPartialDerivative(1, 0, 0),
                y.getPartialDerivative(0, 1, 0),
                y.getPartialDerivative(0, 0, 1)
        };

    }
}

public class vonmisesfitter extends AbstractCurveFitter {
    protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
        final int len = points.size();
        final double[] target = new double[len];
        final double[] weights = new double[len];
        final double[] initialGuess = {1.0, 1.0, 1.0};

        int i = 0;
        for (WeightedObservedPoint point : points) {
            target[i] = point.getY();
            weights[i] = point.getWeight();
            i += 1;
        }

        final AbstractCurveFitter.TheoreticalValuesFunction model = new
                AbstractCurveFitter.TheoreticalValuesFunction(new vonmises(), points);

        return new LeastSquaresBuilder().
                maxEvaluations(Integer.MAX_VALUE).
                maxIterations(Integer.MAX_VALUE).
                start(initialGuess).
                target(target).
                weight(new DiagonalMatrix(weights)).
                model(model.getModelFunction(), model.getModelFunctionJacobian()).
                build();
    }
}
