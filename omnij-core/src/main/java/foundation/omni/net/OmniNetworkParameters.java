package foundation.omni.net;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.NetworkParameters;

/**
 * Base class for Omni Protocol network parameters
 * TODO: Convert from abstract class to interface -- also consider creating a data class to hold
 * these fields so the interface will just return the data class
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
     * @param bitcoinNetwork Bitcoin network enum
     * @return Omni network params for specified Bitcoin network
     */
    public static OmniNetworkParameters fromBitcoinNetwork(Network bitcoinNetwork) {
        if (!(bitcoinNetwork instanceof BitcoinNetwork)) throw new IllegalArgumentException("Unsupported Network");
        switch ((BitcoinNetwork) bitcoinNetwork) {
            case MAINNET:
                return OmniMainNetParams.get();
            case TESTNET:
                return OmniTestNetParams.get();
            case REGTEST:
                return OmniRegTestParams.get();
            default:
                throw new IllegalArgumentException("Unsupported Network");
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
