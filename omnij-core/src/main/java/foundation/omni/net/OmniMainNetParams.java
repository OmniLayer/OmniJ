package foundation.omni.net;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.params.MainNetParams;

/**
 * Omni Protocol parameters for Bitcoin MainNet
 */
public class OmniMainNetParams extends OmniNetworkParameters {
    private static final String ExodusAddress = "1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P";
    public static final Integer FIRST_EXODUS_BLOCK = 249498;
    public static final Integer LAST_EXODUS_BLOCK = 255365;
    public static final Integer POST_EXODUS_BLOCK = 255366;
    public static final Integer MSC_DEX_BLOCK = 290630;
    public static final Integer MSC_SP_BLOCK =297110;

    private OmniMainNetParams() {
        super();
        params = MainNetParams.get();
        try {
            exodusAddress = LegacyAddress.fromString(params, ExodusAddress);
        } catch (AddressFormatException e) {
            exodusAddress = null;
        }
        moneyManAddress = null;
        firstExodusBlock = FIRST_EXODUS_BLOCK;
        lastExodusBlock = LAST_EXODUS_BLOCK;
        postExodusBlock = POST_EXODUS_BLOCK;
        mscDEXBlock = MSC_DEX_BLOCK;
        mscSPBlock = MSC_SP_BLOCK;
    }

    private static OmniMainNetParams instance;
    public static synchronized OmniMainNetParams get() {
        if (instance == null) {
            instance = new OmniMainNetParams();
        }
        return instance;
    }
}
