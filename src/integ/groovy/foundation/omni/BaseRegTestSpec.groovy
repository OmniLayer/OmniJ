package foundation.omni

import com.msgilligan.bitcoin.rpc.RPCURL
import foundation.omni.rpc.MastercoinCLIClient
import foundation.omni.rpc.MastercoinClientDelegate
import foundation.omni.test.TestSupport
import spock.lang.Specification


/**
 * User: sean
 * Date: 7/26/14
 * Time: 7:01 PM
 */
class BaseRegTestSpec extends Specification implements MastercoinClientDelegate, TestSupport {

    static final BigDecimal minBTCForTests = 50.0;

    {
        client = new MastercoinCLIClient(RPCURL.defaultRegTestURL, BaseMainNetSpec.rpcuser, BaseMainNetSpec.rpcpassword)
    }

    void setupSpec() {
        System.err.println("Waiting for server...")
        Boolean available = client.waitForServer(60)   // Wait up to 1 minute
        if (!available) {
            System.err.println("Timeout error.")
        }

        // Set a default transaction fee, so a known reference value can be used in tests
        assert client.setTxFee(stdTxFee)

        // Make sure we have enough test coins
        while (getBalance() < minBTCForTests) {
            // Mine blocks until we have some coins to spend
            client.generateBlocks(1)
        }
    }

    void cleanupSpec() {
        // Nothing to clean up for now
    }

}