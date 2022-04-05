package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import foundation.omni.OmniOutput;

import java.io.IOException;

/**
 * Serializer for record-like {@link OmniOutput}
 */
public class OmniOutputSerializer extends JsonSerializer<OmniOutput> {
    @Override
    public void serialize(OmniOutput value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("address", value.address().toString());
        gen.writeStringField("amount", value.amount().toJsonFormattedString());
        gen.writeEndObject();
    }
}
