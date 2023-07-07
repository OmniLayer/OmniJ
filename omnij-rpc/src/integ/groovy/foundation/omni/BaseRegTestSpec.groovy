package foundation.omni

import foundation.omni.rpc.test.OmniTestClient
import foundation.omni.rpc.test.OmniTestClientAccessor
import groovy.util.logging.Slf4j
import org.bitcoinj.base.BitcoinNetwork
import org.consensusj.bitcoin.json.pojo.NetworkInfo
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.consensusj.bitcoin.jsonrpc.test.FundingSource
import org.consensusj.bitcoin.jsonrpc.test.FundingSourceAccessor
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource
import foundation.omni.rpc.test.TestServers
import foundation.omni.test.OmniTestSupport
import spock.lang.Specification


/**
 * Base specification for integration tests on RegTest net
 */
@Slf4j
abstract class BaseRegTestSpec extends Specification implements OmniTestClientAccessor, FundingSourceAccessor, OmniTestSupport {
    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;
    private static RegTestFundingSource FUNDING_INSTANCE;

    @Delegate
    @Override
    OmniTestClient client() {
        return getClientInstance()
    }

    @Delegate
    @Override
    FundingSource fundingSource() {
        if (FUNDING_INSTANCE == null) {
            FUNDING_INSTANCE = new RegTestFundingSource(client())
        }
        return FUNDING_INSTANCE;
    }

    private static OmniTestClient INSTANCE;

    static synchronized OmniTestClient getClientInstance() {
        // We use a shared client for RegTest integration tests, because we want a single value for regTestMiningAddress
        if (INSTANCE == null) {
            var host = 'localhost'
            var uri = URI.create("http://${host}:18443").resolve("/wallet/${BitcoinExtendedClient.REGTEST_WALLET_NAME}")
            INSTANCE = new OmniTestClient(BitcoinNetwork.REGTEST, uri, rpcTestUser, rpcTestPassword)
            INSTANCE.initRegTestWallet()
        }
        return INSTANCE;
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

    void consolidateCoins() {
        fundingSource.fundingSourceMaintenance();
    }
}
