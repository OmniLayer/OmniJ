package foundation.omni;

/**
 * Master Protocol parameters for Bitcoin MainNet
 */
public class OPTestNetParams extends OPNetworkParameters {
    /* TestNet not supported yet */
    public static synchronized OPMainNetParams get() {
        throw new UnsupportedOperationException();
    }
}
