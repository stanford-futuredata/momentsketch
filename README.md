# Moments Sketch
This repository contains a simplified implementation of the sketch and solver from the Moment-based quantile sketches [paper](https://arxiv.org/abs/1803.01969) and described in the DAWN blog [post](https://dawn.cs.stanford.edu/2018/08/29/moments/). 
This sketch allows for approximate quantile estimates with minimal (< 200bytes) space overhead and extremely fast aggregation speeds.
Compared with the [evaluation code](https://github.com/stanford-futuredata/msketch) used in the paper, this repository has a simpler, cleaner implementation which can be more useful for experimentation and development. 

The momentsolver/ directory contains an implementation of a solver that estimates quantiles given a **moments sketch**: which consists of the min, max, and power sums of a dataset. A [test case](https://github.com/stanford-futuredata/momentsketch/blob/master/momentsolver/src/test/java/momentsketch/MomentSolverTest.java) demonstrates how to use the solver by either adding data values or passing in the relevant statistics directly. 

The solver here attempts to find discretized distributions for efficiency, and one can adjust the grid size to improve the precision of the solver at the cost of some query-time overhead.
As a rule of thumb tracking 15 power sums provides approximately 1 percent CDF error in estimating quantiles, 
and a grid size of 1024 is enough for most purposes while keeping solve times to a millisecond.

The druid-ext/ directory contains code for how one could integrate the sketch as a Druid extension.

## Usage Advice

To simplify implementation, this version of the moments sketch does not track log-moments by default. 
On datasets that range across many orders of magnitude this moments sketch thus has better accuracy when
used together with pre-processing via a __log-transform__. 
If the data has both heavy tails and negative values an __arcsinh__ transforms is useful.
One can write a simple wrapper to transform values before passing them to the sketch and then undoing the transformation
when retrieving the estimated quantiles.
