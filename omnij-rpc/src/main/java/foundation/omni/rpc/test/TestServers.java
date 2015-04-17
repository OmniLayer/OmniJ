package foundation.omni.rpc.test;

import com.msgilligan.bitcoin.rpc.RPCURI;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Test Server Connection Data
 */
public class TestServers {
    public static final String rpcTestUser = System.getProperty("omni.test.rpcTestUser");
    public static final String rpcTestPassword = System.getProperty("omni.test.rpcTestPassword");
    public static final String stableOmniRpcHost = System.getProperty("omni.test.stableOmniRpcHost");
    public static final String stableOmniRpcUser = System.getProperty("omni.test.stableOmniRpcUser");
    public static final String stableOmniRpcPassword = System.getProperty("omni.test.stableOmniRpcPassword");

    public static URI getStablePublicMainNetURI() {
        try {
            return new URI(RPCURI.rpcssl, null, stableOmniRpcHost, RPCURI.RPCPORT_MAINNET, RPCURI.rpcfile, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
