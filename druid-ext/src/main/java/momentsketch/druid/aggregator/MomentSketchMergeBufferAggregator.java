package momentsketch.druid.aggregator;

import io.druid.query.aggregation.BufferAggregator;
import io.druid.query.monomorphicprocessing.RuntimeShapeInspector;
import io.druid.segment.ColumnValueSelector;
import momentsketch.druid.MomentSketchWrapper;

import java.nio.ByteBuffer;

public class MomentSketchMergeBufferAggregator implements BufferAggregator
{
    private final ColumnValueSelector<MomentSketchWrapper> selector;
    private final int size;

    public MomentSketchMergeBufferAggregator(ColumnValueSelector<MomentSketchWrapper> selector, int size)
    {
        this.selector = selector;
        this.size = size;
    }

    @Override
    public void init(ByteBuffer buf, int position)
    {
        MomentSketchWrapper h = new MomentSketchWrapper(size);

        ByteBuffer mutationBuffer = buf.duplicate();
        mutationBuffer.position(position);
        h.toBytes(mutationBuffer);
    }

    @Override
    public void aggregate(ByteBuffer buf, int position)
    {
        MomentSketchWrapper msNext = selector.getObject();
        if (msNext == null) {
            return;
        }
        ByteBuffer mutationBuffer = buf.duplicate();
        mutationBuffer.position(position);

        MomentSketchWrapper ms0 = MomentSketchWrapper.fromBytes(mutationBuffer);
        ms0.merge(msNext);

        mutationBuffer.position(position);
        ms0.toBytes(mutationBuffer);
    }

    @Override
    public Object get(ByteBuffer buf, int position)
    {
        ByteBuffer mutationBuffer = buf.asReadOnlyBuffer();
        mutationBuffer.position(position);
        return MomentSketchWrapper.fromBytes(mutationBuffer);
    }

    @Override
    public float getFloat(ByteBuffer buf, int position)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long getLong(ByteBuffer buf, int position)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public double getDouble(ByteBuffer buf, int position)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close() {}

    @Override
    public void inspectRuntimeShape(RuntimeShapeInspector inspector)
    {
        inspector.visit("selector", selector);
    }
}
