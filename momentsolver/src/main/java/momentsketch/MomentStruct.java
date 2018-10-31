package momentsketch;

import java.util.Arrays;

/**
 * Structure for storing a Moments Sketch.
 */
public class MomentStruct {
    // Unlike in the paper we only store power sums here and omit log moments
    // When the data can be skewed or have outliers one should preprocess the data using either
    // a log transform or an arcsinh transform beforehand.
    public double[] power_sums;
    public double min, max;

    public MomentStruct(
            double[] pSums, double min, double max
    ) {
        power_sums = pSums;
        this.min = min;
        this.max = max;
    }

    public MomentStruct(
            int k
    ) {
        power_sums = new double[k];
        this.min = Double.MAX_VALUE;
        this.max = -Double.MAX_VALUE;
    }

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
