package foundation.omni.netapi.omnicore;

import foundation.omni.rpc.OmniClient;
import foundation.omni.rpc.OmniProxyMethods;
import org.bitcoinj.core.NetworkParameters;

import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

/**
 * Add OmniProxyMethods to OmniClient (which is now already reactive)
 * TODO: add OmniProxy support to OmniClient and remove this class
 */
public class RxOmniClient extends OmniClient implements OmniProxyMethods {
    private final boolean isOmniProxy;

    public RxOmniClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        this(netParams, server, rpcuser, rpcpassword, true, false);
    }

    public RxOmniClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword, boolean useZmq, boolean isOmniProxy) {
        this((SSLSocketFactory)SSLSocketFactory.getDefault(), netParams, server, rpcuser, rpcpassword, useZmq, isOmniProxy);
    }

    public RxOmniClient(SSLSocketFactory sslSocketFactory,  NetworkParameters netParams, URI server, String rpcuser, String rpcpassword, boolean useZmq, boolean isOmniProxy) {
        super(sslSocketFactory, netParams, server, rpcuser, rpcpassword, useZmq);
        this.isOmniProxy = isOmniProxy;
    }

    @Override
    public boolean isOmniProxyServer() {
        return this.isOmniProxy;
    }
}
