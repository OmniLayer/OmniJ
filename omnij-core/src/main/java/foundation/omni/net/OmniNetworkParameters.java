package foundation.omni.net;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

/**
 * Base class for Omni Protocol network parameters
 */
public abstract class OmniNetworkParameters {
    protected NetworkParameters params;
    protected Address exodusAddress;



    protected Address moneyManAddress;
    protected Integer firstExodusBlock;
    protected Integer lastExodusBlock;
    protected Integer postExodusBlock;
    protected Integer mscDEXBlock;
    protected Integer mscSPBlock;

    protected OmniNetworkParameters() {
    }

    /**
     * Get Omni Network params given Bitcoin Network params
     *
     * @param btcNetParms Bitcoin network parameters
     * @return Omni network params for specified Bitcoin network
     */
    public static OmniNetworkParameters fromBitcoinParms(NetworkParameters btcNetParms) {
        switch (btcNetParms.getId()) {
            case NetworkParameters.ID_MAINNET:
                return OmniMainNetParams.get();
            case NetworkParameters.ID_TESTNET:
                return OmniTestNetParams.get();
            case NetworkParameters.ID_REGTEST:
                return OmniRegTestParams.get();
            default:
                throw new IllegalArgumentException("Unsupported NetworkParameters instance");
        }
    }

    public NetworkParameters getParams() {
        return params;
    }

    public Address getExodusAddress() {
        return exodusAddress;
    }

    public Address getMoneyManAddress() {
        return moneyManAddress;
    }

    public Integer getFirstExodusBlock() {
        return firstExodusBlock;
    }

    public Integer getLastExodusBlock() {
        return lastExodusBlock;
    }

    public Integer getPostExodusBlock() {
        return postExodusBlock;
    }

    public Integer getMscDEXBlock() {
        return mscDEXBlock;
    }

    public Integer getMscSPBlock() {
        return mscSPBlock;
    }

}
