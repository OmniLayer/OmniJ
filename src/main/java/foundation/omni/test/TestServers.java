package foundation.omni.test;

import com.msgilligan.bitcoin.rpc.RPCURI;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Test Server Connection Data
 */
public class TestServers {
    // TODO: Password configurations for tests should come from external config
    // In the meantime they should be collected here, and moved to getters
    public static final String rpcTestUser = "bitcoinrpc";
    public static final String rpcTestPassword = "pass";
    public static final String stableOmniRpcHost = "core.stage.merchantcoin.net";
    public static final String stableOmniRpcUser = "xmc-msc-rpc";
    public static final String stableOmniRpcPassword = "emdERDIDE82934$%$";

    public static URI getStablePublicMainNetURI() {
        try {
            return new URI(RPCURI.rpcssl, null, stableOmniRpcHost, RPCURI.RPCPORT_MAINNET, RPCURI.rpcfile, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
