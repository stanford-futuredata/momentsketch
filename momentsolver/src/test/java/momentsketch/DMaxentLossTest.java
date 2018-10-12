package momentsketch;

import momentsketch.optimizer.GenericOptimizer;
import momentsketch.optimizer.NewtonOptimizer;
import org.junit.Test;

import static org.junit.Assert.*;

public class DMaxentLossTest {
    @Test
    public void testTrivial() {
        double m_values[] = {1.0, 0, -1.0/3, 0, -1.0/15, 0, -1.0/35};
        double l_values[] = {0.0, 0, 0, 0, 0, 0, 0};
        double tol = 1e-10;
        DMaxentLoss P = new DMaxentLoss(m_values, 21);
        P.computeAll(l_values, tol);
        assertEquals(m_values.length, P.getGradient().length);
    }

    @Test
    public void testSimple() {
        double n = 20;
        double l0Val = Math.log(1.0/n);
        double[] d_mus = {
                1.        , -0.06277785, -0.4079572 , -0.26898716, -0.1348974 ,
                0.34909762,  0.28072058, -0.00188146, -0.27568291, -0.17049106
        };
        DMaxentLoss P = new DMaxentLoss(
                d_mus,
                21
        );

        double[] l0 = new double[d_mus.length];
        l0[0] = l0Val;
        for (int i = 1 ; i < l0.length; i++) {
            l0[i] = 0;
        }
        GenericOptimizer optimizer = new NewtonOptimizer(P);
        optimizer.setVerbose(false);

        long startTime, endTime;
        int numIter = 1;
        startTime = System.nanoTime();
        for (int i = 0; i < numIter; i++) {
            l0 = optimizer.solve(l0, 1e-6);
        }
        endTime = System.nanoTime();
        double elapsed = (endTime - startTime) / (1.0*numIter);

        assertEquals(-3.6, l0[1], .5);
    }
}