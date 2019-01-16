package com.github.stanfordfuturedata.momentsketch;

import com.github.stanfordfuturedata.momentsketch.optimizer.GenericOptimizer;
import com.github.stanfordfuturedata.momentsketch.optimizer.NewtonOptimizer;

import java.util.Arrays;

/**
 * Interface for estimating quantiles given the statistics in a MomentStruct.
 */
public class MomentSolver {
    private double[] c_moments;
    private double xCenter, xScale;
    private double xMin, xMax;

    private int gridSize = 1024;
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

    /**
     * By increasing the number of grid points the estimates will be more precise
     * but more expensive to compute. Accuracy is still limited by the number of
     * known moments. Usually 1024 is a safe default.
     * @param gs number of grid points
     */
    public void setGridSize(int gs) {
        gridSize = gs;
    }

    /**
     *
     * @param maxIter maximum number of steps of optimization
     */
    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }
    public void setVerbose(boolean flag) {
        verbose = flag;
    }

    /**
     * Run an optimization routine to solve for the best-fit distribution
     * given the statistics in a moments sketch. Call solve() before attempting
     * to extract quantiles using getQuantile(p).
     */
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

    public double[] getQuantiles(double ps[]) {
        double[] cdf = new double[gridSize];
        cdf[0] = 0.0;
        for (int i = 1 ; i < gridSize; i++) {
            cdf[i] = cdf[i-1] + weights[i];
        }

        double[] qs = new double[ps.length];
        for (int i = 0; i < ps.length; i++){
            double p = ps[i];
            int leftIdx = 0;
            int rightIdx = gridSize-1;
            if (p <= cdf[leftIdx]) {
                qs[i] = xs[leftIdx];
                continue;
            }
            if (cdf[rightIdx] <= p) {
                qs[i] = xs[rightIdx];
                continue;
            }
            while (true) {
                if (rightIdx - leftIdx <= 1) {
                    qs[i] = xs[rightIdx];
                    break;
                }
                int midIdx = (leftIdx + rightIdx) / 2;
                if (p <= cdf[midIdx]) {
                    rightIdx = midIdx;
                } else {
                    leftIdx = midIdx;
                }
            }
        }
        return qs;
    }

    /**
     * @param p desired quantile p in [0,1]
     * @return the estimated quantile
     */
    public double getQuantile(double p) {
        double[] cdf = new double[gridSize];
        cdf[0] = 0.0;
        for (int i = 1 ; i < gridSize; i++) {
            cdf[i] = cdf[i-1] + weights[i];
        }

        double lastRank = 0.0;
        double curRank = 0.0;

        int targetIdx = gridSize - 1;
        for (int curIdx = 0; curIdx < gridSize; curIdx++) {
            lastRank = curRank;
            curRank = cdf[curIdx];
            if (curRank >= p) {
                targetIdx = curIdx;
                break;
            }
        }

        double q = xs[targetIdx];
        return q;
    }

    /**
     * @return grid points used in estimating the data distribution
     */
    public double[] getXs() {
        return xs;
    }

    /**
     * @return estimated distribution of data across grid points
     */
    public double[] getWeights() {
        return weights;
    }
}
