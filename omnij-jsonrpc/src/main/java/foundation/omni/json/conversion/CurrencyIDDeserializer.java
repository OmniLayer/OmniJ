package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import foundation.omni.CurrencyID;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

import java.io.IOException;

/**
 *
 */
public class CurrencyIDDeserializer extends JsonDeserializer<CurrencyID> {
    @Override
    public CurrencyID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        switch (token) {
            case VALUE_NUMBER_INT:
                long val = p.getNumberValue().longValue();
                return new CurrencyID(val);
            default:
                ctxt.handleUnexpectedToken(Sha256Hash.class, p);
                return null;
        }
    }
}
