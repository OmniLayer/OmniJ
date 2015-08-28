package foundation.omni.rpc.test;

import com.msgilligan.bitcoinj.rpc.RPCURI;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Test Server Connection Data
 */
public class TestServers extends com.msgilligan.bitcoinj.rpc.test.TestServers {
    private static final TestServers INSTANCE = new TestServers();

    private final String stableOmniRpcHost = System.getProperty("omni.test.stableOmniRpcHost");
    private final String stableOmniRpcUser = System.getProperty("omni.test.stableOmniRpcUser");
    private final String stableOmniRpcPassword = System.getProperty("omni.test.stableOmniRpcPassword");

    public static TestServers getInstance() {
        return INSTANCE;
    }

    public String getStableOmniRpcHost() {
        return stableOmniRpcHost;
    }

    public String getStableOmniRpcUser() {
        return stableOmniRpcUser;
    }

    public String getStableOmniRpcPassword() {
        return stableOmniRpcPassword;
    }

    public URI getStablePublicMainNetURI() {
        try {
            return new URI(RPCURI.rpcssl, null, stableOmniRpcHost, RPCURI.RPCPORT_MAINNET, RPCURI.rpcfile, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
