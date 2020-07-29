package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import foundation.omni.OmniValue;

import java.io.IOException;

/**
 *
 */
public class OmniValueSerializer  extends JsonSerializer<OmniValue> {
    @Override
    public void serialize(OmniValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        // OmniValues are serialized as quoted strings, not JSON Numbers
        // That is what the server requires
        gen.writeString(value.toPlainString());
    }
}
