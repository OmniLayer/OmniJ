package foundation.omni.rpc.test;

import foundation.omni.rpc.OmniClient;
import org.bitcoinj.base.Network;

import java.net.URI;

/**
 * For unit tests that need to send invalid raw Omni transactions.
 */
public class OmniTestClient extends OmniClient implements OmniTestClientMethods {
    public OmniTestClient(Network network, URI server, String rpcuser, String rpcpassword) {
        super(network, server, rpcuser, rpcpassword, false, false);
    }
}
