package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import foundation.omni.rpc.BalanceEntry;
import foundation.omni.rpc.AddressBalanceEntries;
import foundation.omni.rpc.AddressBalanceEntry;

import java.io.IOException;

/**
 *
 */
public class AddressBalanceEntriesDeserializer extends StdDeserializer<AddressBalanceEntries> {

    public AddressBalanceEntriesDeserializer() { super(AddressBalanceEntries.class); }

    @Override
    public AddressBalanceEntries deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        AddressBalanceEntries result = new AddressBalanceEntries();
        JsonDeserializer<Object> entryDeserializer = ctxt.findRootValueDeserializer(ctxt.constructType(AddressBalanceEntry.class));
        JsonToken t;
        try {
            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                // Note: must handle null explicitly here; value deserializers won't
                AddressBalanceEntry entry;

                if (t == JsonToken.START_OBJECT) {
                    entry = (AddressBalanceEntry) entryDeserializer.deserialize(jp, ctxt);
                    if (isNonZeroEntry(entry)) {
                        // Only add entries with balance, reserved, or frozen greater than zero.
                        result.put(entry.getAddress(), new BalanceEntry(entry.getBalance(),
                                                                        entry.getReserved(),
                                                                        entry.getFrozen()));
                    }
                } else {
                    throw new JsonMappingException(jp, "unexpected token");
                }
            }
        } catch (Exception e) {
            throw new JsonMappingException(jp, "error", e);
        }

        return result;
    }

    private boolean isNonZeroEntry(AddressBalanceEntry entry) {
        return  entry.getBalance().getWilletts() != 0 ||
                entry.getReserved().getWilletts() != 0 ||
                entry.getFrozen().getWilletts() != 0;
    }
}
