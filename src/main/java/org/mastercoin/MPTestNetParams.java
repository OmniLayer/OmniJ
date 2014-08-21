package org.mastercoin;

/**
 * Master Protocol parameters for Bitcoin MainNet
 */
public class MPTestNetParams extends MPNetworkParameters {
    /* TestNet not supported yet */
    public static synchronized MPMainNetParams get() {
        throw new UnsupportedOperationException();
    }
}
