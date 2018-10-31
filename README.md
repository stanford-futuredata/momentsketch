# Moments Sketch
This repository contains a simplified implementation of the sketch and solver from the Moment-based quantile sketches [paper](https://arxiv.org/abs/1803.01969) and described in the DAWN blog [post](https://dawn.cs.stanford.edu/2018/08/29/moments/). 
This sketch allows for approximate quantile estimates with minimal (< 200bytes) space overhead and extremely fast aggregation speeds.
Compared with the [evaluation code](https://github.com/stanford-futuredata/msketch) used in the paper, this repository has a simpler, cleaner implementation which can be more useful for experimentation and development. 

The momentsolver/ directory contains an implementation of a solver that estimates quantiles given a **moments sketch**: which consists of the min, max, and power sums of a dataset. A [test case](https://github.com/stanford-futuredata/momentsketch/blob/33c9de7d40bc4e7d8e5bfa6510672e6dbb3b0e74/momentsolver/src/test/java/momentsketch/MomentSolverTest.java#L8) demonstrates how to use the solver by either adding data values or passing in the relevant statistics directly. 

The solver here attempts to find discretized distributions for efficiency, and one can adjust the grid size to improve the precision of the solver at the cost of some query-time overhead.

The druid-ext/ directory contains code for how one could integrate the sketch as a Druid extension.