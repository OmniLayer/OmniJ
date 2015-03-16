package foundation.omni

import com.msgilligan.bitcoin.rpc.RPCURI
import foundation.omni.rpc.OmniCLIClient
import foundation.omni.rpc.OmniClientDelegate
import foundation.omni.test.TestServers
import foundation.omni.test.TestSupport
import groovy.util.logging.Slf4j
import spock.lang.Specification


/**
 * Base specification for integration tests on RegTest net
 */
@Slf4j
class BaseRegTestSpec extends Specification implements OmniClientDelegate, TestSupport {

    static final BigDecimal minBTCForTests = 50.0;

    {
        client = new OmniCLIClient(RPCURI.defaultRegTestURI, TestServers.rpcTestUser, TestServers.rpcTestPassword)
    }

    void setupSpec() {
        log.info "Waiting for server..."
        Boolean available = client.waitForServer(60)   // Wait up to 1 minute
        if (!available) {
            log.error "Timeout error."
        }
        assert available

        // Set a default transaction fee, so a known reference value can be used in tests
        assert client.setTxFee(stdTxFee)

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
