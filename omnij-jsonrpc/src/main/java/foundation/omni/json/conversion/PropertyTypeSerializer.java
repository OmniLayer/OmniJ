package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import foundation.omni.Ecosystem;
import foundation.omni.PropertyType;

import java.io.IOException;

/**
 *
 */
public class PropertyTypeSerializer extends JsonSerializer<PropertyType> {
    @Override
    public void serialize(PropertyType prop, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeNumber(prop.value());
    }
}
