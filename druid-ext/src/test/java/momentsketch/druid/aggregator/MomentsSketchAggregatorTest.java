package momentsketch.druid.aggregator;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stanfordfuturedata.momentsketch.MomentStruct;
import com.google.common.collect.Lists;
import io.druid.data.input.Row;
import io.druid.initialization.DruidModule;
import io.druid.jackson.DefaultObjectMapper;
import io.druid.java.util.common.granularity.Granularities;
import io.druid.java.util.common.guava.Sequence;
import io.druid.java.util.common.guava.Sequences;
import io.druid.query.aggregation.AggregationTestHelper;
import io.druid.query.groupby.GroupByQueryConfig;
import io.druid.query.groupby.GroupByQueryRunnerTest;
import momentsketch.druid.MomentSketchModule;
import momentsketch.druid.MomentSketchWrapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MomentsSketchAggregatorTest {

    private final AggregationTestHelper helper;

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    public MomentsSketchAggregatorTest(final GroupByQueryConfig config) {
        DruidModule module = new MomentSketchModule();
        module.configure(null);
        helper = AggregationTestHelper.createGroupByQueryAggregationTestHelper(
                module.getJacksonModules(), config, tempFolder);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<?> constructorFeeder() {
        final List<Object[]> constructors = Lists.newArrayList();
        for (GroupByQueryConfig config : GroupByQueryRunnerTest.testConfigs()) {
            constructors.add(new Object[]{config});
        }
        return constructors;
    }

    // this is to test Json properties and equals
    @Test
    public void serializeDeserializeFactoryWithFieldName() throws Exception {
        ObjectMapper objectMapper = new DefaultObjectMapper();
        MomentSketchAggregatorFactory factory = new MomentSketchAggregatorFactory("name", "fieldName", 128);

        MomentSketchAggregatorFactory other = objectMapper.readValue(
                objectMapper.writeValueAsString(factory),
                MomentSketchAggregatorFactory.class);

        assertEquals(factory, other);
    }

    @Test
    public void buildingSketchesAtIngestionTime() throws Exception {
        Sequence<Row> seq = helper.createIndexAndRunQueryOnSegment(
                new File(this.getClass().getClassLoader().getResource("doubles_build_data.tsv").getFile()),
                String.join("\n",
                        "{",
                        "  \"type\": \"string\",",
                        "  \"parseSpec\": {",
                        "    \"format\": \"tsv\",",
                        "    \"timestampSpec\": {\"column\": \"timestamp\", \"format\": \"yyyyMMddHH\"},",
                        "    \"dimensionsSpec\": {",
                        "      \"dimensions\": [\"product\"],",
                        "      \"dimensionExclusions\": [ \"sequenceNumber\"],",
                        "      \"spatialDimensions\": []",
                        "    },",
                        "    \"columns\": [\"timestamp\", \"sequenceNumber\", \"product\", \"value\"]",
                        "  }",
                        "}"),
                "[{\"type\": \"momentSketch\", \"name\": \"sketch\", \"fieldName\": \"value\", \"k\": 10}]",
                0, // minTimestamp
                Granularities.NONE,
                10, // maxRowCount
                String.join("\n",
                        "{",
                        "  \"queryType\": \"groupBy\",",
                        "  \"dataSource\": \"test_datasource\",",
                        "  \"granularity\": \"ALL\",",
                        "  \"dimensions\": [],",
                        "  \"aggregations\": [",
                        "    {\"type\": \"momentSketchMerge\", \"name\": \"sketch\", \"fieldName\": \"sketch\", \"k\": 10}",
                        "  ],",
                        "  \"postAggregations\": [",
                        "    {\"type\": \"momentSketchSolveQuantiles\", \"name\": \"quantiles\", \"fractions\": [0, 0.5, 1], \"field\": {\"type\": \"fieldAccess\", \"fieldName\": \"sketch\"}}",
                        "  ],",
                        "  \"intervals\": [\"2016-01-01T00:00:00.000Z/2016-01-31T00:00:00.000Z\"]",
                        "}"));
        List<Row> results = Sequences.toList(seq, Lists.newArrayList());
        assertEquals(1, results.size());
        Row row = results.get(0);
        double[] quantilesArray = (double[]) row.getRaw("quantiles");
        assertEquals(0,   quantilesArray[0], 0.05);
        assertEquals(.5,  quantilesArray[1], 0.05);
        assertEquals(1.0, quantilesArray[2], 0.05);

        MomentSketchWrapper sketchObject = (MomentSketchWrapper) row.getRaw("sketch");
        assertEquals(400.0, sketchObject.getPowerSums()[0], 1e-10);
    }

    @Test
    public void buildingSketchesAtQueryTime() throws Exception {
        Sequence<Row> seq = helper.createIndexAndRunQueryOnSegment(
                new File(this.getClass().getClassLoader().getResource("doubles_build_data.tsv").getFile()),
                String.join("\n",
                        "{",
                        "  \"type\": \"string\",",
                        "  \"parseSpec\": {",
                        "    \"format\": \"tsv\",",
                        "    \"timestampSpec\": {\"column\": \"timestamp\", \"format\": \"yyyyMMddHH\"},",
                        "    \"dimensionsSpec\": {",
                        "      \"dimensions\": [ \"product\"],",
                        "      \"dimensionExclusions\": [\"sequenceNumber\"],",
                        "      \"spatialDimensions\": []",
                        "    },",
                        "    \"columns\": [\"timestamp\", \"sequenceNumber\", \"product\", \"value\"]",
                        "  }",
                        "}"),
                "[{\"type\": \"doubleSum\", \"name\": \"value\", \"fieldName\": \"value\"}]",
                0, // minTimestamp
                Granularities.NONE,
                10, // maxRowCount
                String.join("\n",
                        "{",
                        "  \"queryType\": \"groupBy\",",
                        "  \"dataSource\": \"test_datasource\",",
                        "  \"granularity\": \"ALL\",",
                        "  \"dimensions\": [],",
                        "  \"aggregations\": [",
                        "    {\"type\": \"momentSketch\", \"name\": \"sketch\", \"fieldName\": \"value\", \"k\": 10}",
                        "  ],",
                        "  \"intervals\": [\"2016-01-01T00:00:00.000Z/2016-01-31T00:00:00.000Z\"]",
                        "}"));

        List<Row> results = Sequences.toList(seq, Lists.newArrayList());
        assertEquals(1, results.size());
        Row row = results.get(0);

        MomentSketchWrapper sketchObject = (MomentSketchWrapper) row.getRaw("sketch");
        MomentStruct sketchStruct = sketchObject.data;
        // 9 total products since we pre-sum the values.
        assertEquals(9.0, sketchStruct.power_sums[0], 1e-10);
    }
}

