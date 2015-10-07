package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;

import java.io.IOException;

/**
 *
 */
public class EcosystemSerializer extends JsonSerializer<Ecosystem> {
    @Override
    public void serialize(Ecosystem eco, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeNumber(eco.getValue());
    }
}
