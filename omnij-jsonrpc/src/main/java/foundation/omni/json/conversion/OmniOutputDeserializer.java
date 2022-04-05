package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import foundation.omni.OmniOutput;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import org.bitcoinj.core.Address;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Deserializer for record-like {@link OmniOutput}
 */
public class OmniOutputDeserializer extends JsonDeserializer<OmniOutput> {
    @Override
    public OmniOutput deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);
        String address = node.get("address").asText();
        String amount = node.get("amount").asText();
        return new OmniOutput(Address.fromString(null, address), omniValueFromString(amount));
    }

    static OmniValue omniValueFromString(String val) {
        boolean divisible = val.contains(".");  // Divisible amounts always contain a decimal point
        if (divisible) {
            return OmniValue.of(new BigDecimal(val), PropertyType.DIVISIBLE);
        } else {
            return OmniValue.of(Long.parseLong(val), PropertyType.INDIVISIBLE);
        }
    }
}
