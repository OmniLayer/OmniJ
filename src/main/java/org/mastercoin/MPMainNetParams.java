package org.mastercoin;

public class MPMainNetParams extends MPNetworkParameters {
    public static final String ExodusAddress = "1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P";
    public static final Integer FIRST_EXODUS_BLOCK = 249498;
    public static final Integer LAST_EXODUS_BLOCK = 255365;
    public static final Integer POST_EXODUS_BLOCK = 255366;
    public static final Integer MSC_DEX_BLOCK = 290630;
    public static final Integer MSC_SP_BLOCK =297110;

    private MPMainNetParams() {
        super();
        exodusAddress = ExodusAddress;
        firstExodusBlock = FIRST_EXODUS_BLOCK;
        lastExodusBlock = LAST_EXODUS_BLOCK;
        postExodusBlock = POST_EXODUS_BLOCK;
        mscDEXBlock = MSC_DEX_BLOCK;
        mscSPBlock = MSC_SP_BLOCK;
    }

    private static MPMainNetParams instance;
    public static synchronized MPMainNetParams get() {
        if (instance == null) {
            instance = new MPMainNetParams();
        }
        return instance;
    }
}
