package foundation.omni.net;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.exceptions.AddressFormatException;
import org.bitcoinj.params.TestNet3Params;

/**
 * Omni Protocol parameters for Bitcoin TestNet
 */
public class OmniTestNetParams extends OmniNetworkParameters {
    private final static String MoneyManAddress = "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP";

    private OmniTestNetParams() {
        super();
        params = TestNet3Params.get();
        try {
            exodusAddress = OmniNetwork.TESTNET.exodusAddress();
        } catch (AddressFormatException e) {
            throw new RuntimeException(e);
        }
        try {
            moneyManAddress = OmniNetwork.addressParser.parseAddress(MoneyManAddress, BitcoinNetwork.TESTNET);
        } catch (AddressFormatException e) {
            throw new RuntimeException(e);
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
