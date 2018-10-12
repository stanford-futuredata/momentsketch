package momentsketch.druid.aggregator;

import io.druid.query.aggregation.Aggregator;
import io.druid.segment.ColumnValueSelector;
import momentsketch.druid.MomentSketchWrapper;

public class MomentSketchMergeAggregator implements Aggregator {
    private final ColumnValueSelector<MomentSketchWrapper> selector;
    private MomentSketchWrapper momentsSketch;
    private final int k;

    public MomentSketchMergeAggregator(ColumnValueSelector<MomentSketchWrapper> selector, final int k) {
        this.selector = selector;
        this.momentsSketch = new MomentSketchWrapper(k);
        this.k = k;
    }

    @Override
    public void aggregate() {
        final MomentSketchWrapper sketch = selector.getObject();
        if (sketch == null) {
            return;
        }
        this.momentsSketch.merge(sketch);
    }

    @Override
    public Object get() {
        return momentsSketch;
    }

    @Override
    public float getFloat() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long getLong() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close() {
        momentsSketch = null;
    }
}
