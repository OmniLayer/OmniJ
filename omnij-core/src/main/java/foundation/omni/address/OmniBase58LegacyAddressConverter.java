package foundation.omni.address;

import foundation.omni.net.OmniNetwork;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.LegacyAddress;

/**
 * EXPERIMENTAL 2-way Conversion between MAINNET Omni and BTC Addresses
 */
class OmniBase58LegacyAddressConverter {
    static Address btcToOmni(Address btcAddress) {
        return LegacyAddress.fromPubKeyHash(OmniNetwork.MAINNET, btcAddress.getHash());
    }

    static Address omniToBTC(Address omniAddress) {
        return LegacyAddress.fromPubKeyHash(BitcoinNetwork.MAINNET, omniAddress.getHash());
    }
}
