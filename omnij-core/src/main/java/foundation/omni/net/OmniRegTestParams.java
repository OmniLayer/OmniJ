package foundation.omni.net;

import org.bitcoinj.params.RegTestParams;

/**
 * Omni Protocol parameters for Bitcoin MainNet
 */
public class OmniRegTestParams extends OmniNetworkParameters {

    private OmniRegTestParams() {
        super();
        params = RegTestParams.get();
        exodusAddress = OmniNetwork.REGTEST.exodusAddress();
        moneyManAddress = OmniNetwork.REGTEST.moneyManAddress();
        /* Use MPMainNetParams for magic block numbers */
        firstExodusBlock = 5;
        lastExodusBlock = OmniMainNetParams.LAST_EXODUS_BLOCK;
        postExodusBlock = OmniMainNetParams.POST_EXODUS_BLOCK;
        mscDEXBlock = OmniMainNetParams.MSC_DEX_BLOCK;
        mscSPBlock = OmniMainNetParams.MSC_SP_BLOCK;
    }

    private static OmniRegTestParams instance;
    public static synchronized OmniRegTestParams get() {
        if (instance == null) {
            instance = new OmniRegTestParams();
        }
        return instance;
    }
}
