package org.mastercoin;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.NetworkParameters;

/**
 * User: sean
 * Date: 7/22/14
 * Time: 1:07 AM
 */
public abstract class MPNetworkParameters {
    protected NetworkParameters params;
    protected Address exodusAddress;



    protected Address moneyManAddress;
    protected Integer firstExodusBlock;
    protected Integer lastExodusBlock;
    protected Integer postExodusBlock;
    protected Integer mscDEXBlock;
    protected Integer mscSPBlock;

    protected MPNetworkParameters() {
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
