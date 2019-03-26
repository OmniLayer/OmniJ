package foundation.omni.net;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.params.TestNet3Params;

/**
 * Omni Protocol parameters for Bitcoin TestNet
 */
public class OmniTestNetParams extends OmniNetworkParameters {
    private final static String ExodusAddress = "mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv";
    private final static String MoneyManAddress = "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP";

    private OmniTestNetParams() {
        super();
        params = TestNet3Params.get();
        try {
            exodusAddress = LegacyAddress.fromString(params, ExodusAddress);
        } catch (AddressFormatException e) {
            exodusAddress = null;
        }
        try {
            moneyManAddress = LegacyAddress.fromString(params, MoneyManAddress);
        } catch (AddressFormatException e) {
            moneyManAddress = null;
        }
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
