package foundation.omni.net;

import org.bitcoinj.params.TestNet3Params;

/**
 * Omni Protocol parameters for Bitcoin TestNet
 */
public class OmniTestNetParams extends OmniNetworkParameters {

    private OmniTestNetParams() {
        super();
        params = TestNet3Params.get();
        exodusAddress = OmniNetwork.TESTNET.exodusAddress();
        moneyManAddress = OmniNetwork.TESTNET.moneyManAddress();
        /* TODO: Find out magic block numbers for TestNet */
        firstExodusBlock = 5;
        lastExodusBlock = -1;
        postExodusBlock = -1;
        mscDEXBlock = -1;
        mscSPBlock = -1;
    }

    private static OmniTestNetParams instance;
    public static synchronized OmniTestNetParams get() {
        if (instance == null) {
            instance = new OmniTestNetParams();
        }
        return instance;
    }
}
