package momentsketch;

import momentsketch.optimizer.GenericOptimizer;
import momentsketch.optimizer.NewtonOptimizer;

public class MomentSolver {
    private double[] c_moments;
    private double xCenter, xScale;
    private double xMin, xMax;

    private int gridSize = 21;
    private int maxIter = 15;
    private boolean verbose = false;

    private double[] xs;
    private double[] lambd;
    private double[] weights;

    public MomentSolver(
            MomentStruct ms
    ) {
        xMin = ms.min;
        xMax = ms.max;
        xCenter = (xMax + xMin)/2;
        xScale = (xMax - xMin)/2;
        c_moments = MathUtil.powerSumsToChebyMoments(
                xMin, xMax, ms.power_sums
        );
    }

    public void setGridSize(int gs) {
        gridSize = gs;
    }
    public void setVerbose(boolean flag) {
        verbose = flag;
    }

    public void solve() {
        int n = gridSize;
        DMaxentLoss P = new DMaxentLoss(c_moments, gridSize);

        double[] l0 = new double[c_moments.length];
        l0[0] = Math.log(1.0 / n);
        for (int i = 1; i < l0.length; i++) {
            l0[i] = 0;
        }
        GenericOptimizer optimizer = new NewtonOptimizer(P);
        optimizer.setVerbose(verbose);
        optimizer.setMaxIter(maxIter);

        lambd = optimizer.solve(l0, 1e-6);
        P.computeAll(lambd, 0.0);
        weights = P.getWeights();

        xs = new double[gridSize];
        for (int i = 0; i < gridSize; i++) {
            double scaledX = i*2.0/(gridSize-1)-1.0;
            xs[i] = scaledX*xScale + xCenter;
        }
    }

    public double getQuantile(double p) {
        return 0.0;
    }
    public double[] getXs() {
        return xs;
    }
    public double[] getWeights() {
        return weights;
    }
}
