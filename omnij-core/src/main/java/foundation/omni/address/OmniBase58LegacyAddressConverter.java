package foundation.omni.address;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.params.MainNetParams;

/**
 * EXPERIMENTAL 2-way Conversion between MAINNET Omni and BTC Addresses
 */
public class OmniBase58LegacyAddressConverter {
    static final OmniAddressMainNetParams omniParams = OmniAddressMainNetParams.get();
    static final MainNetParams btcParams = MainNetParams.get();

    static Address btcToOmni(Address btcAddress) {
        return LegacyAddress.fromPubKeyHash(omniParams, btcAddress.getHash());
    }

    static Address omniToBTC(Address omniAddress) {
        return LegacyAddress.fromPubKeyHash(btcParams, omniAddress.getHash());
    }
}
