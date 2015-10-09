package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.SimpleType;
import foundation.omni.rpc.AddressBalanceEntries;
import foundation.omni.rpc.AddressBalanceEntry;
import foundation.omni.rpc.BalanceEntry;

import java.io.IOException;
import java.math.BigDecimal;

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
                        // Only add entries with balance or reserved greater than zero.
                        result.put(entry.getAddress(), new BalanceEntry(entry.getBalance(), entry.getReserved()));
                    }
                } else {
                    throw new JsonMappingException("unexpected token");
                }
            }
        } catch (Exception e) {
            throw new JsonMappingException("error", e);
        }

        return result;
    }

    private boolean isNonZeroEntry(AddressBalanceEntry entry) {
        return entry.getBalance().compareTo(BigDecimal.ZERO) == 1 || entry.getReserved().compareTo(BigDecimal.ZERO) == 1;
    }
}
