package momentsketch.druid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MomentSketchWrapperTest {
    @Test
    public void testDeserialize() {
        MomentSketchWrapper mw = new MomentSketchWrapper(10);
        mw.setCompressed(false);
        mw.add(10);
        byte[] bs = mw.toByteArray();
        MomentSketchWrapper mw2 = MomentSketchWrapper.fromByteArray(bs);

        assertEquals(10, mw2.getPowerSums()[1], 1e-10);
    }
}
