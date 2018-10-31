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

    @Test
    public void testMilanLogData() {
        // Log transform works very well on milan data
        double minData = -12.96899970738978;
        double maxData = 8.979198088411168;
        double[] powerSums = {
                24308139.0, 12420049.706757378, 293245315.4299223, 131886982.50259101,
                6781335850.764435, -6571640034.562504, 253168798811.8452, -919129472417.1187,
                14293827594309.152, -93264716269145.2, 1113140641657524.2, -9711395910128630.0,
                1.085213731386595e+17, -1.0979641590548122e+18, 1.232426498322159e+19
        };
        MomentStruct mData = new MomentStruct(
                powerSums,
                minData,
                maxData
        );
        long startTime = System.nanoTime();

        int numIter = 1;
        MomentSolver ms = new MomentSolver(mData);
        ms.setGridSize(1024);
        for (int i = 0; i < numIter; i++) {
            ms.solve();
        }

        long elapsed = System.nanoTime() - startTime;
        double timePer = elapsed*1.0 / numIter;
        double q = Math.exp(ms.getQuantile(.90));
        assertTrue(q < 100);
        assertTrue(q > 90);
    }

    @Test
    public void testMilanArcsinhData() {
        // arcsinh transform can handle negative values but compresses small values
        // for the milan dataset.
        double minData = 2.331497699527218e-06;
        double maxData = 9.672345272940357;
        double[] powerSums = {
                24308139.0, 54890011.69268947, 238580708.62136176, 1150204745.6709106,
                5912306163.506206, 32001600745.32434, 181021599632.44467, 1064120737505.5359,
                6471014895729.757, 40550499825482.09, 260983399634733.6, 1720144478337448.0,
                1.1581465001793776e+16, 7.948216615871363e+16, 5.549795612843805e+17
        };
        MomentStruct mData = new MomentStruct(
                powerSums,
                minData,
                maxData
        );
        MomentSolver ms = new MomentSolver(mData);
        ms.setGridSize(10000);
        ms.solve();
        double q = Math.sinh(ms.getQuantile(.9));
        assertTrue(q < 100);
        assertTrue(q > 90);
    }
}