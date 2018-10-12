package momentsketch;

import momentsketch.optimizer.FunctionWithHessian;
import org.apache.commons.math3.util.FastMath;

public class DMaxentLoss implements FunctionWithHessian  {
    protected int dim;
    protected int nGrid;
    protected double[] d_mus;

    protected double[] xs;
    protected double[][] cpVals;

    protected double[] lambd;
    protected double[] weights;
    protected double[] mus;
    protected double[] grad;
    protected double[][] hess;

    public DMaxentLoss(
            double[] d_mus,
            int nGrid
    ) {
        dim = d_mus.length;
        this.nGrid = nGrid;
        this.d_mus = d_mus;

        xs = new double[nGrid];
        for (int i = 0; i < nGrid; i++) {
            xs[i] = i*2.0/(nGrid-1)-1.0;
        }

        cpVals = new double[2*dim][nGrid];
        for (int i = 0; i < nGrid; i++) {
            cpVals[0][i] = 1.0;
            cpVals[1][i] = xs[i];
        }
        for (int j = 2; j < 2*dim; j++) {
            for (int i = 0; i < nGrid; i++) {
                cpVals[j][i] = 2*xs[i]*cpVals[j-1][i] - cpVals[j-2][i];
            }
        }

        int k = dim;
        this.weights = new double[nGrid];
        this.mus = new double[2*k];
        this.grad = new double[k];
        this.hess = new double[k][k];
    }

    public void setLambd(double[] newLambd) {
        lambd = newLambd;
    }

    @Override
    public void computeOnlyValue(double[] point, double tol) {
        computeAll(point, tol);
    }

    @Override
    public void computeAll(double[] point, double tol) {
        setLambd(point);
        for (int i = 0; i < nGrid; i++){
            double sum = 0.0;
            for (int j = 0; j < dim; j++){
                sum += lambd[j]*cpVals[j][i];
            }
            weights[i] = FastMath.exp(sum);
        }
        for (int i = 0; i < 2*dim; i++){
            double sum = 0.0;
            for (int j = 0; j < nGrid; j++) {
                sum += cpVals[i][j]*weights[j];
            }
            mus[i] = sum;
        }
        for (int i = 0; i < dim; i++) {
            grad[i] = mus[i] - d_mus[i];
        }

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                hess[i][j] = .5*(mus[i+j] + mus[FastMath.abs(i-j)]);
            }
        }
    }

    public double[] getWeights() {
        return weights;
    }

    @Override
    public int dim() {
        return dim;
    }

    @Override
    public double getValue() {
        double sum = 0.0;
        int k = d_mus.length;
        for (int i = 0; i < k; i++) {
            sum += lambd[i] * d_mus[i];
        }
        return this.mus[0] - sum;
    }

    @Override
    public double[] getGradient() {
        return grad;
    }

    @Override
    public double[][] getHessian() {
        return hess;
    }
}
