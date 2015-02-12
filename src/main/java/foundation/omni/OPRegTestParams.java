package foundation.omni;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.params.RegTestParams;

/**
 * Master Protocol parameters for Bitcoin MainNet
 */
public class OPRegTestParams extends OPNetworkParameters {
    private final static String ExodusAddress = "mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv";
    private final static String MoneyManAddress = "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP";

    private OPRegTestParams() {
        super();
        params = RegTestParams.get();
        try {
            exodusAddress = new Address(params, ExodusAddress);
        } catch (AddressFormatException e) {
            exodusAddress = null;
        }
        try {
            moneyManAddress = new Address(params, MoneyManAddress);
        } catch (AddressFormatException e) {
            moneyManAddress = null;
        }
        /* Use MPMainNetParams for magic block numbers */
        firstExodusBlock = 5;
        lastExodusBlock = OPMainNetParams.LAST_EXODUS_BLOCK;
        postExodusBlock = OPMainNetParams.POST_EXODUS_BLOCK;
        mscDEXBlock = OPMainNetParams.MSC_DEX_BLOCK;
        mscSPBlock = OPMainNetParams.MSC_SP_BLOCK;
    }

    private static OPRegTestParams instance;
    public static synchronized OPRegTestParams get() {
        if (instance == null) {
            instance = new OPRegTestParams();
        }
        return instance;
    }
}
