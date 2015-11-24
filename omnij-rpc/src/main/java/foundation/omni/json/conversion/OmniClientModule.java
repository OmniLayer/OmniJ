package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.msgilligan.bitcoinj.json.conversion.AddressDeserializer;
import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import foundation.omni.rpc.AddressBalanceEntries;
import foundation.omni.rpc.PropertyBalanceEntries;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

/**
 *
 */
public class OmniClientModule extends SimpleModule {
    public OmniClientModule(NetworkParameters netParams) {
        super("OmniJMappingClient", new Version(1, 0, 0, null, null, null));
        this.addDeserializer(CurrencyID.class, new CurrencyIDDeserializer())
            .addDeserializer(AddressBalanceEntries.class, new AddressBalanceEntriesDeserializer())
            .addDeserializer(OmniValue.class, new OmniValueDeserializer())
            .addDeserializer(PropertyBalanceEntries.class, new PropertyBalanceEntriesDeserializer())
            .addSerializer(CurrencyID.class, new CurrencyIDSerializer())
            .addSerializer(Ecosystem.class, new EcosystemSerializer())
            .addSerializer(PropertyType.class, new PropertyTypeSerializer())
            .addSerializer(OmniValue.class, new OmniValueSerializer());
    }
}
