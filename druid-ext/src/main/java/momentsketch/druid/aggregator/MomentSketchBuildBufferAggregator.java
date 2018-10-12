package momentsketch.druid.aggregator;

import io.druid.query.aggregation.BufferAggregator;
import io.druid.query.monomorphicprocessing.RuntimeShapeInspector;
import io.druid.segment.ColumnValueSelector;
import io.druid.segment.DoubleColumnSelector;
import momentsketch.MomentStruct;
import momentsketch.druid.MomentSketchWrapper;

import java.nio.ByteBuffer;

public class MomentSketchBuildBufferAggregator implements BufferAggregator
{
    private final ColumnValueSelector<Double> selector;
    private final int k;

    public MomentSketchBuildBufferAggregator(final ColumnValueSelector<Double> valueSelector, final int k)
    {
        this.selector = valueSelector;
        this.k = k;
    }

    @Override
    public synchronized void init(final ByteBuffer buffer, final int position)
    {
        ByteBuffer mutationBuffer = buffer.duplicate();
        mutationBuffer.position(position);

        MomentSketchWrapper emptyStruct = new MomentSketchWrapper(k);
        emptyStruct.toBytes(mutationBuffer);
    }

    @Override
    public synchronized void aggregate(final ByteBuffer buffer, final int position)
    {
        ByteBuffer mutationBuffer = buffer.duplicate();
        mutationBuffer.position(position);

        MomentSketchWrapper ms0 = MomentSketchWrapper.fromBytes(mutationBuffer);
        double x = selector.getDouble();
        ms0.add(x);

        mutationBuffer.position(position);
        ms0.toBytes(mutationBuffer);
    }

    @Override
    public synchronized Object get(final ByteBuffer buffer, final int position)
    {
        ByteBuffer mutationBuffer = buffer.duplicate();
        mutationBuffer.position(position);
        return MomentSketchWrapper.fromBytes(mutationBuffer);
    }

    @Override
    public float getFloat(final ByteBuffer buffer, final int position)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long getLong(final ByteBuffer buffer, final int position)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public synchronized void close() {}
}
