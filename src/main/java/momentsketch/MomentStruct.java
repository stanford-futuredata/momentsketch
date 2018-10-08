package momentsketch;

public class MomentStruct {
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

    public void add(double[] xVals) {
        for (double x : xVals) {
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
    }
}
