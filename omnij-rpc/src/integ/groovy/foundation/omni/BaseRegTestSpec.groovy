package foundation.omni

import com.msgilligan.bitcoinj.json.pojo.NetworkInfo
import com.msgilligan.jsonrpc.Loggable
import com.msgilligan.bitcoinj.rpc.RPCURI
import com.msgilligan.bitcoinj.test.RegTestFundingSource
import foundation.omni.rpc.OmniCLIClient
import foundation.omni.rpc.OmniClientDelegate
import foundation.omni.rpc.test.OmniTestClient
import foundation.omni.rpc.test.TestServers
import foundation.omni.test.OmniTestClientDelegate
import foundation.omni.test.OmniTestSupport
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.RegTestParams
import spock.lang.Specification


/**
 * Base specification for integration tests on RegTest net
 */
abstract class BaseRegTestSpec extends Specification implements OmniTestClientDelegate, OmniTestSupport, Loggable {

    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;

    {
        client = new OmniTestClient(RegTestParams.get(), RPCURI.defaultRegTestURI, rpcTestUser, rpcTestPassword)
        fundingSource = new RegTestFundingSource(client)
    }

    void setupSpec() {
        Boolean available = client.waitForServer(60)   // Wait up to 1 minute
        if (!available) {
            log.error "Timeout error."
        }
        assert available

        // Set and confirm default fees, so a known reference value can be used in tests
        assert client.setTxFee(stdTxFee)
        NetworkInfo basicinfo = client.getNetworkInfo()
        // TODO: How to update the following 2 asserts given that getinfo is deprecated
        //assert basicinfo.paytxfee == stdTxFee
        //assert basicinfo.relayFee == stdRelayTxFee.value
    }

    void cleanupSpec() {
        // Spend almost all coins as fee, to sweep dust
        consolidateCoins()
    }

}
