package foundation.omni;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

/**
 * Base class for Master Protocol network parameters
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
