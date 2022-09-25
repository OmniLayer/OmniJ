package foundation.omni.rpc.test;

import foundation.omni.rpc.OmniClient;
import org.bitcoinj.core.NetworkParameters;

import java.net.URI;

/**
 * For unit tests that need to send invalid raw Omni transactions.
 */
public class OmniTestClient extends OmniClient implements OmniTestClientMethods {
    public OmniTestClient(NetworkParameters netParams, URI server, String rpcuser, String rpcpassword) {
        super(netParams, server, rpcuser, rpcpassword, false, false);
    }
}
