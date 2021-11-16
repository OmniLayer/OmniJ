package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import foundation.omni.CurrencyID;

import java.io.IOException;

/**
 * Serialize a CurrencyID to a number string (we can't use toString, because it doesn't produce a pure number)
 */
public class CurrencyIDStringSerializer extends JsonSerializer<CurrencyID> {
    @Override
    public void serialize(CurrencyID id, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeFieldName(Long.toString(id.getValue()));
    }
}
