package foundation.omni.address;

import org.bitcoinj.params.RegTestParams;

/**
 * EXPERIMENTAL Subclass of RegTestParams for generating Omni Addresses
 */
public class OmniAddressRegTestParams extends RegTestParams {
    public OmniAddressRegTestParams() {
        super();
        segwitAddressHrp = "ort";   // Human-readable-part for Omni-Layer Bech32 addresses
    }


    private static OmniAddressRegTestParams instance;
    public static synchronized OmniAddressRegTestParams get() {
        if (instance == null) {
            instance = new OmniAddressRegTestParams();
        }
        return instance;
    }
}
