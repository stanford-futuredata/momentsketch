# Moments Sketch
This repository contains a simplified implementation of the sketch and solver from the Moment-based quantile sketches [paper](https://arxiv.org/abs/1803.01969) and described in the DAWN blog [post](https://dawn.cs.stanford.edu/2018/08/29/moments/). 
This sketch allows for approximate quantile estimates with minimal (< 200bytes) space overhead and extremely fast aggregation speeds.
Compared with the [evaluation code](https://github.com/stanford-futuredata/msketch) used in the paper, this repository has a simpler, cleaner implementation which can be more useful for experimentation and development. 

The momentsolver/ directory contains an implementation of a solver that estimates quantiles given a **moments sketch**: which consists of the min, max, and power sums of a dataset. A [test case](https://github.com/stanford-futuredata/momentsketch/blob/master/momentsolver/src/test/java/momentsketch/MomentSolverTest.java) demonstrates how to use the solver by either adding data values or passing in the relevant statistics directly. 

The key parameter during usage is the number of power sums (moments) tracked by the sketch. 
As a rule of thumb 15 moments provides on average 1 percent CDF error in estimating quantiles on our benchmark datasets.
The solver for estimating quantiles from the sketch is additionally parameterized by gridSize and maxIter (max iterations).
These affect the precision and final query time, but do not affect aggregation time.
For most purposes default values of gridSize=1024 and maxIter=15 should be sufficient.

The grid size defines the granularity of the discretized distributions used to estimate the quantiles from the sketch, while the max iteration count sets a cap on how many Newton steps the solver will take to reach convergence.

The druid-ext/ directory contains code for how one could integrate the sketch as a Druid extension.

## Usage Advice

To simplify implementation, this version of the moments sketch does not track log-moments by default. 
On datasets that range across many orders of magnitude this moments sketch thus has better accuracy when
used together with pre-processing via a __log-transform__. 
If the data has both heavy tails and negative values an __arcsinh__ transforms is useful.
One can write a simple wrapper to transform values before passing them to the sketch and then undoing the transformation
when retrieving the estimated quantiles.
