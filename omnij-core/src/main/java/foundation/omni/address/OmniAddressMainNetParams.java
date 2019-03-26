package foundation.omni.address;

import org.bitcoinj.params.MainNetParams;

/**
 * EXPERIMENTAL Subclass of MainNetParams for generating Omni Addresses
 * This is an experiment for POC Omni-specific addresses
 * If we go ahead with this approach we should merge `OmniAddressMainNetParams` with `OmniMainNetParams`
 */
public class OmniAddressMainNetParams extends MainNetParams {
    public OmniAddressMainNetParams() {
        super();
        addressHeader = 115;    // 'o'
        p2shHeader = 58;        // 'Q'
    }


    private static OmniAddressMainNetParams instance;
    public static synchronized OmniAddressMainNetParams get() {
        if (instance == null) {
            instance = new OmniAddressMainNetParams();
        }
        return instance;
    }
}
