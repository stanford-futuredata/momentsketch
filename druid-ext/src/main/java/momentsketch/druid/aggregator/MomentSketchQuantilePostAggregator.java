package momentsketch.druid.aggregator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.druid.java.util.common.IAE;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.aggregation.AggregatorUtil;
import io.druid.query.aggregation.PostAggregator;
import io.druid.query.cache.CacheKeyBuilder;
import momentsketch.druid.MomentSketchWrapper;

import java.util.Comparator;
import java.util.*;

public class MomentSketchQuantilePostAggregator implements PostAggregator {
    private final String name;
    private final PostAggregator field;
    private final double[] fractions;

    public static final String TYPE_NAME = "momentSketchSolveQuantiles";

    @JsonCreator
    public MomentSketchQuantilePostAggregator(
            @JsonProperty("name") final String name,
            @JsonProperty("field") final PostAggregator field,
            @JsonProperty("fractions") final double[] fractions)
    {
        this.name = Preconditions.checkNotNull(name, "name is null");
        this.field = Preconditions.checkNotNull(field, "field is null");
        this.fractions = Preconditions.checkNotNull(fractions, "array of fractions is null");
    }

    @Override
    @JsonProperty
    public String getName()
    {
        return name;
    }

    @JsonProperty
    public PostAggregator getField()
    {
        return field;
    }

    @JsonProperty
    public double[] getFractions()
    {
        return fractions;
    }

    @Override
    public Object compute(final Map<String, Object> combinedAggregators)
    {
        final MomentSketchWrapper sketch = (MomentSketchWrapper) field.compute(combinedAggregators);
        double[] quantiles = sketch.getQuantiles(fractions);
        return quantiles;
    }

    @Override
    public Comparator<double[]> getComparator()
    {
        throw new IAE("Comparing arrays of quantiles is not supported");
    }

    @Override
    public Set<String> getDependentFields()
    {
        return field.getDependentFields();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", field=" + field +
                ", fractions=" + Arrays.toString(fractions) +
                "}";
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MomentSketchQuantilePostAggregator that = (MomentSketchQuantilePostAggregator) o;
        if (!name.equals(that.name)) {
            return false;
        }
        if (!Arrays.equals(fractions, that.fractions)) {
            return false;
        }
        return field.equals(that.field);
    }

    @Override
    public int hashCode()
    {
        return (name.hashCode() * 31 + field.hashCode()) * 31 + Arrays.hashCode(fractions);
    }

    @Override
    public byte[] getCacheKey()
    {
        final CacheKeyBuilder builder = new CacheKeyBuilder(
                AggregatorUtil.QUANTILES_DOUBLES_SKETCH_TO_QUANTILES_CACHE_TYPE_ID).appendCacheable(field);
        for (final double value : fractions) {
            builder.appendDouble(value);
        }
        return builder.build();
    }

    @Override
    public PostAggregator decorate(final Map<String, AggregatorFactory> map)
    {
        return this;
    }

}
