package momentsketch;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MomentSolverTest {
    @Test
    public void testSimple() {
        double[] powerSums = {200000.0, 304719.1420864805, 618596.450341607, 1361890.949909549, 3102323.794140877, 7200195.90117825, 16918271.75568709, 40122284.67435465, 95873463.0010957, 230593433.48962364};
        MomentStruct mData = new MomentStruct(
                powerSums,
                2.0496528630116753e-05,
                3.2187884808801686
        );
        MomentSolver ms = new MomentSolver(mData);
        ms.solve();
        System.out.println(Arrays.toString(ms.getXs()));
        System.out.println(Arrays.toString(ms.getWeights()));
    }

}