package momentsketch.druid;

import io.druid.data.input.InputRow;
import io.druid.java.util.common.IAE;
import io.druid.segment.GenericColumnSerializer;
import io.druid.segment.column.ColumnBuilder;
import io.druid.segment.data.GenericIndexed;
import io.druid.segment.data.ObjectStrategy;
import io.druid.segment.serde.ComplexColumnPartSupplier;
import io.druid.segment.serde.ComplexMetricExtractor;
import io.druid.segment.serde.ComplexMetricSerde;
import io.druid.segment.serde.LargeColumnSupportedComplexColumnSerializer;
import io.druid.segment.writeout.SegmentWriteOutMedium;
import momentsketch.druid.aggregator.MomentSketchAggregatorFactory;

import java.nio.ByteBuffer;

public class MomentSketchComplexMetricSerde extends ComplexMetricSerde {
    private static final MomentSketchObjectStrategy strategy = new MomentSketchObjectStrategy();

    @Override
    public String getTypeName(){
        return MomentSketchAggregatorFactory.TYPE_NAME;
    }

    @Override
    public ComplexMetricExtractor getExtractor() {
        return new ComplexMetricExtractor()
        {
            @Override
            public Class<?> extractedClass()
            {
                return MomentSketchWrapper.class;
            }

            @Override
            public Object extractValue(final InputRow inputRow, final String metricName)
            {
                Object rawValue = inputRow.getRaw(metricName);
                if (rawValue instanceof MomentSketchWrapper) {
                    return (MomentSketchWrapper) rawValue;
                } else {
                    throw new IAE("Not a momentSketch");
                }
            }
        };
    }

    @Override
    public void deserializeColumn(ByteBuffer buffer, ColumnBuilder builder) {
        final GenericIndexed<MomentSketchWrapper> column = GenericIndexed.read(
                buffer, strategy, builder.getFileMapper()
        );
        builder.setComplexColumn(new ComplexColumnPartSupplier(getTypeName(), column));
    }

    @Override
    public ObjectStrategy getObjectStrategy() {
        return strategy;
    }

    @Override
    public GenericColumnSerializer getSerializer(SegmentWriteOutMedium segmentWriteOutMedium, String column)
    {
        return LargeColumnSupportedComplexColumnSerializer.create(
                segmentWriteOutMedium, column, this.getObjectStrategy()
        );
    }

}
