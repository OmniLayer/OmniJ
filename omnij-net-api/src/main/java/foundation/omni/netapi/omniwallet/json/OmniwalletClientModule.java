package foundation.omni.netapi.omniwallet.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bitcoinj.base.Network;
import org.consensusj.bitcoin.json.conversion.AddressDeserializer;
import org.consensusj.bitcoin.json.conversion.AddressKeyDeserializer;
import org.consensusj.bitcoin.json.conversion.AddressSerializer;
import org.consensusj.bitcoin.json.conversion.Sha256HashDeserializer;
import org.consensusj.bitcoin.json.conversion.Sha256HashSerializer;
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
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Sha256Hash;

import java.util.Objects;

/**
 * A module of Jackson converters for OmniwalletClient that provides all converters necessary for an Omniwallet Client.
 */
public class OmniwalletClientModule extends SimpleModule {

    /**
     * Construct a Jackson converter module with all converters necessary
     * for an Omniwallet Client.
     *
     */
    public OmniwalletClientModule() {
        this(null);
    }

    /**
     * Construct a Jackson converter module with all converters necessary for an Omniwallet Client
     * that enforces valid address formats for the specified {@link Network}.
     *
     * @param network Specifies the network to validate addresses for
     */
    public OmniwalletClientModule(Network network) {
        super("OmniJOWMappingClient", new Version(1, 0, 0, null, null, null));
        this    .addKeyDeserializer(Address.class, network != null ? new AddressKeyDeserializer(network) : new AddressKeyDeserializer())
                .addDeserializer(Address.class, network != null ? new AddressDeserializer(network) : new AddressDeserializer())
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
