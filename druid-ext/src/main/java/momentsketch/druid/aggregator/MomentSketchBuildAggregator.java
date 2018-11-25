package momentsketch.druid.aggregator;

import io.druid.query.aggregation.Aggregator;
import io.druid.segment.ColumnValueSelector;
import momentsketch.druid.MomentSketchWrapper;

public class MomentSketchBuildAggregator implements Aggregator {
    private final ColumnValueSelector<Double> valueSelector;
    private final int k;

    private MomentSketchWrapper momentsSketch;

    public MomentSketchBuildAggregator(final ColumnValueSelector<Double> valueSelector, final int size) {
        this.valueSelector = valueSelector;
        this.k = size;
        momentsSketch = new MomentSketchWrapper(k);
    }

    @Override
    public void aggregate() {
        momentsSketch.add(valueSelector.getDouble());
    }

    @Override
    public Object get() {
        return momentsSketch;
    }

    @Override
    public float getFloat() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public long getLong() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Aggregator clone()
    {
        return new MomentSketchBuildAggregator(valueSelector, k);
    }

    @Override
    public void close() {
        momentsSketch = null;
    }
}
