package foundation.omni.netapi.omniwallet.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.msgilligan.bitcoinj.json.conversion.AddressDeserializer;
import com.msgilligan.bitcoinj.json.conversion.AddressKeyDeserializer;
import com.msgilligan.bitcoinj.json.conversion.AddressSerializer;
import com.msgilligan.bitcoinj.json.conversion.Sha256HashDeserializer;
import com.msgilligan.bitcoinj.json.conversion.Sha256HashSerializer;
import foundation.omni.CurrencyID;
import foundation.omni.Ecosystem;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import foundation.omni.json.conversion.CurrencyIDDeserializer;
import foundation.omni.json.conversion.CurrencyIDSerializer;
import foundation.omni.json.conversion.EcosystemSerializer;
import foundation.omni.json.conversion.OmniValueDeserializer;
import foundation.omni.json.conversion.OmniValueSerializer;
import foundation.omni.json.conversion.PropertyTypeSerializer;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;

/**
 * A module of Jackson converters for OmniwalletClient
 */
public class OmniwalletClientModule extends SimpleModule {
    public OmniwalletClientModule(NetworkParameters netParams) {
        super("OmniJOWMappingClient", new Version(1, 0, 0, null, null, null));
        this    .addKeyDeserializer(Address.class, new AddressKeyDeserializer(netParams))
                .addDeserializer(Address.class, new AddressDeserializer(netParams))
                .addDeserializer(CurrencyID.class, new CurrencyIDDeserializer())
                .addDeserializer(OmniValue.class, new OmniValueDeserializer())
                .addDeserializer(Sha256Hash.class, new Sha256HashDeserializer())
                .addSerializer(Address.class, new AddressSerializer())
                .addSerializer(CurrencyID.class, new CurrencyIDSerializer())
                .addSerializer(Ecosystem.class, new EcosystemSerializer())
                .addSerializer(PropertyType.class, new PropertyTypeSerializer())
                .addSerializer(OmniValue.class, new OmniValueSerializer())
                .addSerializer(Sha256Hash.class, new Sha256HashSerializer());
    }
}
