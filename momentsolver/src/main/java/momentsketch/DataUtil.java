package momentsketch;

import java.util.List;

public class DataUtil {
    public static double[] listToArray(List<Double> xList) {
        int n = xList.size();
        double[] xs = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = xList.get(i);
        }
        return xs;
    }
}
