package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.PropertyBalanceEntries;
import foundation.omni.rpc.PropertyBalanceEntry;

import java.io.IOException;

/**
 *
 */
public class PropertyBalanceEntriesDeserializer extends StdDeserializer<PropertyBalanceEntries> {

    public PropertyBalanceEntriesDeserializer() { super(PropertyBalanceEntries.class); }

    @Override
    public PropertyBalanceEntries deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        PropertyBalanceEntries result = new PropertyBalanceEntries();
        JsonDeserializer<Object> entryDeserializer = ctxt.findRootValueDeserializer(ctxt.constructType(PropertyBalanceEntry.class));
        JsonToken t;
        try {
            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                // Note: must handle null explicitly here; value deserializers won't
                PropertyBalanceEntry entry;

                if (t == JsonToken.START_OBJECT) {
                    entry = (PropertyBalanceEntry) entryDeserializer.deserialize(jp, ctxt);
                    result.put(entry.getPropertyid(), new BalanceEntry( entry.getBalance(),
                                                                        entry.getReserved(),
                                                                        entry.getFrozen()));
                } else {
                    throw new JsonMappingException(jp, "unexpected token");
                }
            }
        } catch (Exception e) {
            throw new JsonMappingException(jp, "error", e);
        }

        return result;
    }
}
