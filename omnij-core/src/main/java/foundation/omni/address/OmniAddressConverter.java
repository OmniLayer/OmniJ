package foundation.omni.address;

import org.bitcoinj.core.Address;
import org.bitcoinj.params.MainNetParams;

/**
 * EXPERIMENTAL 2-way Conversion between MAINNET Omni and BTC Addresses
 */
public class OmniAddressConverter {
    static final OmniAddressMainNetParams omniParams = OmniAddressMainNetParams.get();
    static final MainNetParams btcParams = MainNetParams.get();

    static Address btcToOmni(Address btcAddress) {
        return new Address(omniParams, btcAddress.getHash160());
    }

    static Address omniToBTC(Address omniAddress) {
        return new Address(btcParams, omniAddress.getHash160());
    }
}
