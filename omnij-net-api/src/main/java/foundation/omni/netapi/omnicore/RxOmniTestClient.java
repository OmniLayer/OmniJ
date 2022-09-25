package foundation.omni.netapi.omnicore;

import foundation.omni.rpc.OmniClient;
import foundation.omni.rpc.test.OmniTestClientMethods;
import org.bitcoinj.core.NetworkParameters;

import java.net.URI;

/**
 * For unit tests that need to send invalid raw Omni transactions.
 */
public class RxOmniTestClient extends OmniClient implements OmniTestClientMethods {
    public RxOmniTestClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword, false, false);
    }
}
