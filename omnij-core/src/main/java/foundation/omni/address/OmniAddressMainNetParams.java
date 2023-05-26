package foundation.omni.address;

import foundation.omni.net.OmniNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.params.MainNetParams;

/**
 * EXPERIMENTAL Subclass of MainNetParams for generating Omni Addresses
 * This is an experiment for POC Omni-specific addresses
 * If we go ahead with this approach we should merge `OmniAddressMainNetParams` with `OmniMainNetParams`
 */
public class OmniAddressMainNetParams extends MainNetParams {
    private OmniAddressMainNetParams() {
        super();
        addressHeader = 115;    // 'o' prefix for Base58 P2PKH (deprecated)
        p2shHeader = 58;        // 'Q' prefix for Base58 P2SH (deprecated)
        segwitAddressHrp = "o"; // Human-readable-part for Omni-Layer Bech32 addresses
    }


    private static OmniAddressMainNetParams instance;

    /**
     * @return The singleton instance
     */
    public static synchronized OmniAddressMainNetParams get() {
        if (instance == null) {
            instance = new OmniAddressMainNetParams();
        }
        return instance;
    }

    @Override
    public Network network() {
        return OmniNetwork.MAINNET;
    }
}
