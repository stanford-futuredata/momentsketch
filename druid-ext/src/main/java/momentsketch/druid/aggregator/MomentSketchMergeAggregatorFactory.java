package momentsketch.druid.aggregator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.druid.query.aggregation.Aggregator;
import io.druid.query.aggregation.BufferAggregator;
import io.druid.segment.ColumnSelectorFactory;
import io.druid.segment.ColumnValueSelector;
import momentsketch.druid.MomentSketchWrapper;

public class MomentSketchMergeAggregatorFactory extends MomentSketchAggregatorFactory
{
    public static final String TYPE_NAME = "momentSketchMerge";
    private static final byte MOMENTS_SKETCH_MERGE_CACHE_ID = 0x52;

    @JsonCreator
    public MomentSketchMergeAggregatorFactory(
            @JsonProperty("name") final String name,
            @JsonProperty("k") final Integer k)
    {
        super(name, name, k, MOMENTS_SKETCH_MERGE_CACHE_ID);
    }

    @Override
    public Aggregator factorize(final ColumnSelectorFactory metricFactory)
    {
        final ColumnValueSelector<MomentSketchWrapper> selector = metricFactory.makeColumnValueSelector(
                getFieldName());
        return new MomentSketchMergeAggregator(selector, getK());
    }

    @Override
    public BufferAggregator factorizeBuffered(final ColumnSelectorFactory metricFactory)
    {
        final ColumnValueSelector<MomentSketchWrapper> selector = metricFactory.makeColumnValueSelector(
                getFieldName()
        );
        return new MomentSketchMergeBufferAggregator(selector, getK());
    }

}
