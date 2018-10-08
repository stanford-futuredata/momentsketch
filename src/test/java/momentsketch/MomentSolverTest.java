package momentsketch;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.junit.Test;

import static org.junit.Assert.*;

public class MomentSolverTest {
    @Test
    public void testSimple() {
        double[] powerSums = {
                200000.0, 304719.1420864805, 618596.450341607, 1361890.949909549,
                3102323.794140877, 7200195.90117825, 16918271.75568709, 40122284.67435465,
                95873463.0010957, 230593433.48962364
        };
        MomentStruct mData = new MomentStruct(
                powerSums,
                2.0496528630116753e-05,
                3.2187884808801686
        );
        MomentSolver ms = new MomentSolver(mData);
        ms.setGridSize(51);
        ms.solve();
        double q = ms.getQuantile(.9);
        assertEquals(2.46, q, .1);
    }

    @Test
    public void testFromRaw() {
        int n = 1000;
        double[] xVals = new double[n];
        for (int i = 0 ; i < n; i++) {
            xVals[i] = i;
        }
        MomentStruct mData = new MomentStruct(10);
        mData.add(xVals);

        MomentSolver ms = new MomentSolver(mData);
        ms.setGridSize(1024);
        ms.solve();
        double q = ms.getQuantile(.9);

        Percentile p = new Percentile();
        p.setData(xVals);
        double truep90 = p.evaluate(90.0);
        assertEquals(truep90, q, 1.0);
    }

}