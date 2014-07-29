package org.mastercoin;

/**
 * User: sean
 * Date: 7/22/14
 * Time: 1:22 AM
 */
public class MPRegTestParams extends MPNetworkParameters {
    public final static String ExodusAddress = "mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv";
    public final static String MoneyManAddress = "moneyqMan7uh8FqdCA2BV5yZ8qVrc9ikLP";

    private MPRegTestParams() {
        super();
        exodusAddress = ExodusAddress;
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
