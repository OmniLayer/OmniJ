package foundation.omni

import com.msgilligan.bitcoin.rpc.Loggable
import com.msgilligan.bitcoin.rpc.RPCURI
import foundation.omni.rpc.OmniCLIClient
import foundation.omni.rpc.OmniClientDelegate
import foundation.omni.tx.RawTxBuilder
import foundation.omni.rpc.test.TestServers
import foundation.omni.test.OmniTestSupport
import spock.lang.Specification


/**
 * Base specification for integration tests on RegTest net
 */
class BaseRegTestSpec extends Specification implements OmniClientDelegate, OmniTestSupport, Loggable {

    static final BigDecimal minBTCForTests = 50.0;

    {
        client = new OmniCLIClient(RPCURI.defaultRegTestURI, TestServers.rpcTestUser, TestServers.rpcTestPassword)
        builder = new RawTxBuilder()
    }

    void setupSpec() {
        log.info "Waiting for server..."
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

        // Make sure we have enough test coins
        while (getBalance() < minBTCForTests) {
            // Mine blocks until we have some coins to spend
            client.generateBlocks(1)
        }
    }

    void cleanupSpec() {
        // Spend almost all coins as fee, to sweep dust
        consolidateCoins()
    }

}
