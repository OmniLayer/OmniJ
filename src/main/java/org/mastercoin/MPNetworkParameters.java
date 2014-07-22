package org.mastercoin;

/**
 * User: sean
 * Date: 7/22/14
 * Time: 1:07 AM
 */
public abstract class MPNetworkParameters {
    protected String  exodusAddress;
    protected Integer firstExodusBlock;
    protected Integer lastExodusBlock;
    protected Integer postExodusBlock;
    protected Integer mscDEXBlock;
    protected Integer mscSPBlock;

    protected MPNetworkParameters() {
    }

    public String getExodusAddress() {
        return exodusAddress;
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
