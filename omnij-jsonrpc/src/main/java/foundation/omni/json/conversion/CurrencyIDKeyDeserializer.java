package foundation.omni.json.conversion;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import foundation.omni.CurrencyID;

/**
 * Deserialization toCurrencyID for when it's used in as a map key
 */
public class CurrencyIDKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
        return CurrencyID.of(Long.parseLong(key));
    }
}
