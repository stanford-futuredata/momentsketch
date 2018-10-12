package momentsketch.druid;

import momentsketch.MomentSolver;
import momentsketch.MomentStruct;

import java.nio.ByteBuffer;

public class MomentSketchWrapper {
    public MomentStruct data;
    // Whether we use arcsinh to compress the range
    public boolean useArcSinh = true;

    public MomentSketchWrapper(
            int k
    ) {
        data = new MomentStruct(k);
    }

    public MomentSketchWrapper(
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

    public void merge(MomentSketchWrapper other) {
        data.merge(other.data);
    }

    public byte[] toByteArray() {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES+(data.power_sums.length+2)*Double.BYTES);
        return toBytes(bb).array();
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
            quantiles[i] = Math.sinh(rawQuantile);
        }

        return quantiles;
    }

    public ByteBuffer toBytes(ByteBuffer bb) {
        bb.putInt(data.power_sums.length);
        bb.putDouble(data.min);
        bb.putDouble(data.max);
        for (double x : data.power_sums) {
            bb.putDouble(x);
        }
        return bb;
    }

    public static MomentSketchWrapper fromBytes(ByteBuffer bb) {
        int k = bb.getInt();
        MomentStruct m = new MomentStruct(k);
        m.min = bb.getDouble();
        m.max = bb.getDouble();
        for (int i = 0; i < k; i++) {
            m.power_sums[i] = bb.getDouble();
        }
        return new MomentSketchWrapper(m);
    }

    public static MomentSketchWrapper fromByteArray(byte[] input) {
        ByteBuffer bb = ByteBuffer.wrap(input);
        return fromBytes(bb);
    }

    public String toString() {
        return data.toString();
    }
}
