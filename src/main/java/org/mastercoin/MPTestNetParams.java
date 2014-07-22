package org.mastercoin;

/**
 * User: sean
 * Date: 7/22/14
 * Time: 8:25 AM
 */
public class MPTestNetParams extends MPNetworkParameters {
    /* TestNet not supported yet */
    public static synchronized MPMainNetParams get() {
        throw new UnsupportedOperationException();
    }
}
