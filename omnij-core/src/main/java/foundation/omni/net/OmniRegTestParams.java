package foundation.omni.net;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.DefaultAddressParser;
import org.bitcoinj.base.exceptions.AddressFormatException;
import org.bitcoinj.base.LegacyAddress;
import org.bitcoinj.params.RegTestParams;

/**
 * Omni Protocol parameters for Bitcoin MainNet
 */
public class OmniRegTestParams extends OmniNetworkParameters {
    private final static String ExodusAddress = "mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv";
    private final static String MoneyManAddress = "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP";
    private static final AddressParser parser = new DefaultAddressParser();

    private OmniRegTestParams() {
        super();
        params = RegTestParams.get();
        try {
            exodusAddress = parser.parseAddress(ExodusAddress, params.network());
        } catch (AddressFormatException e) {
            exodusAddress = null;
        }
        try {
            moneyManAddress = parser.parseAddress(MoneyManAddress, params.network());
        } catch (AddressFormatException e) {
            moneyManAddress = null;
        }
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
