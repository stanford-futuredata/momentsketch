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
}
