package org.mastercoin;

/**
 * User: sean
 * Date: 7/22/14
 * Time: 1:22 AM
 */
public class MPRegTestParams extends MPNetworkParameters {

    private MPRegTestParams() {
        super();
        exodusAddress = MPMainNetParams.ExodusAddress;
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
