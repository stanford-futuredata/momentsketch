package com.github.stanfordfuturedata.momentsketch;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleMomentSketchTest {
    @Test
    public void testSimple() {
        int n = 1000;
        double[] xVals = new double[n];
        for (int i = 0 ; i < n; i++) {
            xVals[i] = i;
        }

        SimpleMomentSketch ms = new SimpleMomentSketch(10);
        ms.setCompressed(true);
        for (double x : xVals) {
            ms.add(x);
        }
        double[] ps = {.5, .9};
        double[] qs = ms.getQuantiles(ps);

        assertEquals(500, qs[0], 10);
        assertEquals(900, qs[1], 10);
    }
}