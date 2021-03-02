package foundation.omni.json.conversion;

import com.msgilligan.bitcoinj.json.conversion.AddressSerializer;
import com.msgilligan.bitcoinj.json.conversion.CoinSerializer;
import com.msgilligan.bitcoinj.json.conversion.ECKeySerializer;
import com.msgilligan.bitcoinj.json.conversion.PeerSerializer;
import com.msgilligan.bitcoinj.json.conversion.Sha256HashSerializer;
import com.msgilligan.bitcoinj.json.conversion.TransactionSerializer;
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
                .addSerializer(Coin.class, new CoinSerializer())
                .addSerializer(ECKey.class, new ECKeySerializer())
                .addSerializer(Peer.class, new PeerSerializer())
                .addSerializer(Sha256Hash.class, new Sha256HashSerializer())
                .addSerializer(Transaction.class, new TransactionSerializer());
    }

}
