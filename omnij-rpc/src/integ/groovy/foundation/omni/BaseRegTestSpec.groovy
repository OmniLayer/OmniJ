package foundation.omni

import com.msgilligan.bitcoin.rpc.Loggable
import com.msgilligan.bitcoin.rpc.RPCURI
import foundation.omni.rpc.OmniCLIClient
import foundation.omni.rpc.OmniClientDelegate
import foundation.omni.rpc.test.TestServers
import foundation.omni.test.OmniTestSupport
import spock.lang.Specification


/**
 * Base specification for integration tests on RegTest net
 */
abstract class BaseRegTestSpec extends Specification implements OmniClientDelegate, OmniTestSupport, Loggable {

    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;

    {
        client = new OmniCLIClient(RPCURI.defaultRegTestURI, rpcTestUser, rpcTestPassword)
    }

    void setupSpec() {
        log.debug "Waiting for server..."
        Boolean available = client.waitForServer(60)   // Wait up to 1 minute
        if (!available) {
            log.error "Timeout error."
        }
        assert available

        // Set and confirm default fees, so a known reference value can be used in tests
        assert client.setTxFee(stdTxFee)
        def basicinfo = client.getinfo()
        assert basicinfo['paytxfee'] == stdTxFee
        assert basicinfo['relayfee'] == stdRelayTxFee
    }

    void cleanupSpec() {
        // Spend almost all coins as fee, to sweep dust
        consolidateCoins()
    }

}
