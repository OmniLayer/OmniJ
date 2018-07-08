package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 */
public class CurrencyIDSerializer extends JsonSerializer<CurrencyID> {
    @Override
    public void serialize(CurrencyID id, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeNumber(id.getValue());
    }
}
