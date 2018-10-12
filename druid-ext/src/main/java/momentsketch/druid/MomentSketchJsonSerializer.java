package momentsketch.druid;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import momentsketch.MomentStruct;

import java.io.IOException;

public class MomentSketchJsonSerializer extends JsonSerializer<MomentSketchWrapper> {
    @Override
    public void serialize(
            MomentSketchWrapper momentsSketch, JsonGenerator jsonGenerator, SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeBinary(momentsSketch.toByteArray());
    }
}
