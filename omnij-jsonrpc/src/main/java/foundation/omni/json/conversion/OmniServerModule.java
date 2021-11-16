package foundation.omni.json.conversion;

import foundation.omni.CurrencyID;
import org.consensusj.bitcoin.json.conversion.AddressSerializer;
import org.consensusj.bitcoin.json.conversion.CoinSerializer;
import org.consensusj.bitcoin.json.conversion.ECKeySerializer;
import org.consensusj.bitcoin.json.conversion.PeerSerializer;
import org.consensusj.bitcoin.json.conversion.Sha256HashSerializer;
import org.consensusj.bitcoin.json.conversion.TransactionSerializer;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

/**
 * Extend {@link OmniClientModule} with serializers needed for server functionality.
 */
public class OmniServerModule extends OmniClientModule {
    public OmniServerModule() {
        super();
        this.addSerializer(Address.class, new AddressSerializer())
                .addSerializer(CurrencyID.class, new CurrencyIDSerializer())
                .addSerializer(Coin.class, new CoinSerializer())
                .addSerializer(ECKey.class, new ECKeySerializer())
                .addSerializer(Peer.class, new PeerSerializer())
                .addSerializer(Sha256Hash.class, new Sha256HashSerializer())
                .addSerializer(Transaction.class, new TransactionSerializer());
    }

}
