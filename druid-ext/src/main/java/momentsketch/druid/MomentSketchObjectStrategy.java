package momentsketch.druid;

import io.druid.segment.data.ObjectStrategy;
import momentsketch.druid.aggregator.MomentSketchAggregatorFactory;

import java.nio.ByteBuffer;

public class MomentSketchObjectStrategy implements ObjectStrategy<MomentSketchWrapper> {
    private static final byte[] EMPTY_BYTES = new byte[] {};
    public static final MomentSketchWrapper EMPTY_SKETCH = new MomentSketchWrapper(1);


    @Override
    public Class<? extends MomentSketchWrapper> getClazz() {
        return MomentSketchWrapper.class;
    }

    @Override
    public MomentSketchWrapper fromByteBuffer(ByteBuffer buffer, int numBytes) {
        if (numBytes == 0) {
            return EMPTY_SKETCH;
        }
        buffer.limit(buffer.position() + numBytes);
        return MomentSketchWrapper.fromBytes(buffer);
    }

    @Override
    public byte[] toBytes(MomentSketchWrapper val) {
        if (val == null) {
            return EMPTY_BYTES;
        }
        return val.toByteArray();
    }

    @Override
    public int compare(MomentSketchWrapper o1, MomentSketchWrapper o2) {
        return MomentSketchAggregatorFactory.COMPARATOR.compare(o1, o2);
    }
}
