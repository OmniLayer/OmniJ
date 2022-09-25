package foundation.omni.netapi.omnicore;

import foundation.omni.rpc.OmniClient;
import org.bitcoinj.core.NetworkParameters;

import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

/**
 * Add OmniProxyMethods to OmniClient (which is now already reactive)
 * @deprecated Use {@link OmniClient}
 */
@Deprecated
public class RxOmniClient extends OmniClient  {

    public RxOmniClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword, true, false);
    }

    public RxOmniClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword, boolean useZmq, boolean isOmniProxy) {
        super((SSLSocketFactory)SSLSocketFactory.getDefault(), netParams, server, rpcuser, rpcpassword, useZmq, isOmniProxy);
    }

    public RxOmniClient(SSLSocketFactory sslSocketFactory,  NetworkParameters netParams, URI server, String rpcuser, String rpcpassword, boolean useZmq, boolean isOmniProxy) {
        super(sslSocketFactory, netParams, server, rpcuser, rpcpassword, useZmq, isOmniProxy);
    }
}
