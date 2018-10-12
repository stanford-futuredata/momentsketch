package momentsketch.druid.aggregator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Doubles;
import io.druid.java.util.common.IAE;
import io.druid.java.util.common.ISE;
import io.druid.query.aggregation.Aggregator;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.aggregation.AggregatorFactoryNotMergeableException;
import io.druid.query.aggregation.BufferAggregator;
import io.druid.query.cache.CacheKeyBuilder;
import io.druid.segment.ColumnSelectorFactory;
import io.druid.segment.ColumnValueSelector;
import io.druid.segment.column.ColumnCapabilities;
import io.druid.segment.column.ValueType;
import momentsketch.druid.MomentSketchWrapper;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MomentSketchAggregatorFactory extends AggregatorFactory {
    public static final int DEFAULT_K = 11;

    private final String name;
    private final String fieldName;
    private final int k;
    private final byte cacheTypeId;

    private static final byte MOMENTS_SKETCH_CACHE_ID = 0x51;

    public static final String TYPE_NAME = "momentSketch";

    @JsonCreator
    public MomentSketchAggregatorFactory(
            @JsonProperty("name") final String name,
            @JsonProperty("fieldName") final String fieldName,
            @JsonProperty("k") final Integer k)
    {
        this(name, fieldName, k, MOMENTS_SKETCH_CACHE_ID);
    }

    MomentSketchAggregatorFactory(final String name, final String fieldName, final Integer k, final byte cacheTypeId)
    {
        if (name == null) {
            throw new IAE("Must have a valid, non-null aggregator name");
        }
        this.name = name;
        if (fieldName == null) {
            throw new IAE("Parameter fieldName must be specified");
        }
        this.fieldName = fieldName;
        this.k = k == null ? DEFAULT_K : k;
        this.cacheTypeId = cacheTypeId;
    }


    @Override
    public byte[] getCacheKey()
    {
        return new CacheKeyBuilder(
                cacheTypeId
        ).appendString(name).appendString(fieldName).appendInt(k).build();
    }


    @Override
    public Aggregator factorize(ColumnSelectorFactory metricFactory) {
        ColumnCapabilities cap = metricFactory.getColumnCapabilities(fieldName);
        if (cap == null || ValueType.isNumeric(cap.getType())) {
            final ColumnValueSelector<Double> selector = metricFactory.makeColumnValueSelector(fieldName);
            return new MomentSketchBuildAggregator(selector, k);
        } else {
            final ColumnValueSelector<MomentSketchWrapper> selector = metricFactory.makeColumnValueSelector(fieldName);
            return new MomentSketchMergeAggregator(selector, k);
        }
    }

    @Override
    public BufferAggregator factorizeBuffered(ColumnSelectorFactory metricFactory) {
        ColumnCapabilities cap = metricFactory.getColumnCapabilities(fieldName);
        if (cap == null || ValueType.isNumeric(cap.getType())) {
            final ColumnValueSelector<Double> selector = metricFactory.makeColumnValueSelector(fieldName);
            return new MomentSketchBuildBufferAggregator(selector, k);
        } else {
            final ColumnValueSelector<MomentSketchWrapper> selector = metricFactory.makeColumnValueSelector(fieldName);
            return new MomentSketchMergeBufferAggregator(selector, k);
        }
    }

    public static final Comparator<MomentSketchWrapper> COMPARATOR = Comparator.comparingDouble(
            a -> a.getPowerSums()[0]
    );

    @Override
    public Comparator getComparator() {
        return COMPARATOR;
    }

    @Override
    public Object combine(Object lhs, Object rhs) {
        MomentSketchWrapper union = (MomentSketchWrapper) lhs;
        union.merge((MomentSketchWrapper) rhs);
        return union;
    }

    @Override
    public AggregatorFactory getCombiningFactory()
    {
        return new MomentSketchMergeAggregatorFactory(name, k);
    }

    @Override
    public AggregatorFactory getMergingFactory(AggregatorFactory other) throws AggregatorFactoryNotMergeableException
    {
        if (other.getName().equals(this.getName()) && this.getClass() == other.getClass()) {
            return getCombiningFactory();
        } else {
            throw new AggregatorFactoryNotMergeableException(this, other);
        }
    }

    @Override
    public List<AggregatorFactory> getRequiredColumns() {
        return Collections.<AggregatorFactory> singletonList(
                new MomentSketchAggregatorFactory(
                        fieldName,
                        fieldName,
                        k)
        );
    }

    private MomentSketchWrapper deserializeFromByteArray(byte[] bytes) {
        return MomentSketchWrapper.fromByteArray(bytes);
    }

    @Override
    public Object deserialize(Object serializedSketch) {
        if (serializedSketch instanceof String) {
            String str = (String)serializedSketch;
            return deserializeFromByteArray(Base64.decodeBase64(str.getBytes(Charsets.UTF_8)));
        } else if (serializedSketch instanceof byte[]) {
            return deserializeFromByteArray((byte[]) serializedSketch);
        } else if (serializedSketch instanceof MomentSketchWrapper) {
            return (MomentSketchWrapper) serializedSketch;
        }
        throw new ISE(
                "Object is not of a type that can be deserialized to a Moments Sketch"
                        + serializedSketch.getClass());

    }

    @Override
    public Object finalizeComputation(Object object) {
        return object;
    }

    @Override
    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getFieldName() {
        return fieldName;
    }

    @JsonProperty
    public int getK() {
        return k;
    }

    @Override
    public List<String> requiredFields() {
        return Collections.singletonList(fieldName);
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public int getMaxIntermediateSize() {
        return (2*k+5)* Doubles.BYTES+20;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final MomentSketchAggregatorFactory that = (MomentSketchAggregatorFactory) o;

        return Objects.equals(name, that.name) &&
                Objects.equals(fieldName, that.fieldName) &&
                k == that.k;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, fieldName, k);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{"
                + "name=" + name
                + ", fieldName=" + fieldName
                + ", k=" + k
                + "}";
    }

}
