package foundation.omni;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.params.RegTestParams;

/**
 * Master Protocol parameters for Bitcoin MainNet
 */
public class MPRegTestParams extends MPNetworkParameters {
    private final static String ExodusAddress = "mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv";
    private final static String MoneyManAddress = "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP";

    private MPRegTestParams() {
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
        lastExodusBlock = MPMainNetParams.LAST_EXODUS_BLOCK;
        postExodusBlock = MPMainNetParams.POST_EXODUS_BLOCK;
        mscDEXBlock = MPMainNetParams.MSC_DEX_BLOCK;
        mscSPBlock = MPMainNetParams.MSC_SP_BLOCK;
    }

    private static MPRegTestParams instance;
    public static synchronized MPRegTestParams get() {
        if (instance == null) {
            instance = new MPRegTestParams();
        }
        return instance;
    }
}
