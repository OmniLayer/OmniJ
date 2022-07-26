package foundation.omni.address;

import org.bitcoinj.params.TestNet3Params;

/**
 * EXPERIMENTAL Subclass of TestNet3Params for generating Omni Addresses
 */
public class OmniAddressTestNetParams extends TestNet3Params {
    private OmniAddressTestNetParams() {
        super();
        segwitAddressHrp = "to";    // Human-readable-part for Omni-Layer Bech32 addresses
    }


    private static OmniAddressTestNetParams instance;

    /**
     * @return The singleton instance
     */
    public static synchronized OmniAddressTestNetParams get() {
        if (instance == null) {
            instance = new OmniAddressTestNetParams();
        }
        return instance;
    }
}
