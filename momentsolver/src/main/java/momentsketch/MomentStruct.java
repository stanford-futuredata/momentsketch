package momentsketch;

import java.util.Arrays;

/**
 * Structure for storing the statistics in a Moments Sketch.
 *
 * Unlike in the paper we only store power sums here and omit log moments.
 * When the data can be skewed or have outliers one should preprocess the data using either
 * a log transform or an arcsinh transform beforehand.
 */
public class MomentStruct {
    public double[] power_sums;
    public double min, max;

    /**
     * Initialize a sketch with pre-computed statistics
     * @param pSums sums of powers of data values sum x^i starting with the count i=0
     * @param min the minimum observed value
     * @param max the maximum observed value
     */
    public MomentStruct(
            double[] pSums, double min, double max
    ) {
        power_sums = pSums;
        this.min = min;
        this.max = max;
    }

    /**
     * @param k number of moments to track. 2 <= k <= 20 is the useful range.
     */
    public MomentStruct(
            int k
    ) {
        power_sums = new double[k];
        this.min = Double.MAX_VALUE;
        this.max = -Double.MAX_VALUE;
    }

    /**
     * @param x value to add to the sketch
     */
    public void add(double x) {
        if (x < min) {
            min = x;
        }
        if (x > max) {
            max = x;
        }
        double curPow = 1.0;
        int k = power_sums.length;
        for (int i = 0; i < k; i++)  {
            power_sums[i] += curPow;
            curPow *= x;
        }
    }

    public void add(double[] xVals) {
        for (double x : xVals) {
            add(x);
        }
    }

    /**
     * @param other existing moment sketch structure to aggregate into current structure
     */
    public void merge(MomentStruct other) {
        if (other.min < min) {
            this.min = other.min;
        }
        if (other.max > max) {
            this.max = other.max;
        }
        int k = power_sums.length;
        for (int i = 0; i<k; i++) {
            power_sums[i] += other.power_sums[i];
        }
    }

    @Override
    public String toString() {
        return String.format("%g:%g:%s", min, max, Arrays.toString(power_sums));
    }

}
