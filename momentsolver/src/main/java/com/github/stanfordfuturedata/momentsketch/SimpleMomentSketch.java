package com.github.stanfordfuturedata.momentsketch;


/**
 * Example wrapper class for exposing an approximate quantiles sketch API
 * Uses the MomentSolver and MomentStruct internally and optionally compressed
 * the input range using arcsinh.
 */
public class SimpleMomentSketch {
    public MomentStruct data;
    // Whether we use arcsinh to compress the range
    public boolean useArcSinh = true;

    public SimpleMomentSketch(
            int k
    ) {
        data = new MomentStruct(k);
    }

    public SimpleMomentSketch(
            MomentStruct data
    ) {
        this.data = data;
    }

    public void setCompressed(boolean flag) {
        useArcSinh = flag;
    }
    public boolean getCompressed() {
        return useArcSinh;
    }

    public int getK() {
        return data.power_sums.length;
    }

    public double[] getPowerSums() {
        return data.power_sums;
    }

    public double getMin() {
        return data.min;
    }

    public double getMax() {
        return data.max;
    }

    public void add(double rawX) {
        double x = rawX;
        if (useArcSinh) {
            x = Math.log(rawX + Math.sqrt(1+rawX*rawX));
        }
        data.add(x);
    }

    public void merge(SimpleMomentSketch other) {
        data.merge(other.data);
    }

    public MomentSolver getSolver() {
        MomentSolver ms = new MomentSolver(data);
        return ms;
    }

    public double[] getQuantiles(double[] fractions) {
        MomentSolver ms = new MomentSolver(data);
        ms.setGridSize(1024);
        ms.setMaxIter(15);
        ms.solve();

        double[] quantiles = new double[fractions.length];
        for (int i = 0; i < fractions.length; i++) {
            double rawQuantile = ms.getQuantile(fractions[i]);
            if (useArcSinh) {
              quantiles[i] = Math.sinh(rawQuantile);
            } else {
              quantiles[i] = rawQuantile;
            }
        }

        return quantiles;
    }

    public String toString() {
        return data.toString();
    }
}
