package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 */
public class OmniValueDeserializer extends JsonDeserializer<OmniValue> {
    @Override
    public OmniValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_STRING:
                String val = p.getValueAsString();
                boolean divisible = val.contains(".");  // Divisible amounts always contain a decimal point
                if (divisible) {
                    return OmniValue.of(new BigDecimal(val), PropertyType.DIVISIBLE);
                } else {
                    return OmniValue.of(p.getValueAsLong(), PropertyType.INDIVISIBLE);
                }
            default:
                ctxt.handleUnexpectedToken(Sha256Hash.class, p);
                return null;

        }
    }
}
