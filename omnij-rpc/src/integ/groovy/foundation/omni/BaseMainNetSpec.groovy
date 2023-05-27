package foundation.omni

import foundation.omni.rpc.OmniClient
import groovy.util.logging.Slf4j
import org.bitcoinj.base.BitcoinNetwork
import org.consensusj.bitcoin.jsonrpc.groovy.BlockchainDotInfoSyncing
import org.consensusj.bitcoin.jsonrpc.RpcURI
import foundation.omni.rpc.test.TestServers
import spock.lang.Specification

/**
 * Base specification for integration tests on Main net
 *
 * Creates an RPC client ({@link OmniClient}), waits for
 * RPC server to be responding (typical integration/functional requests require starting
 * an RPC server which can take minutes or even hours) and to be in sync with the main
 * Bitcoin Blockchain.
 *
 */
@Slf4j
abstract class BaseMainNetSpec extends Specification implements BlockchainDotInfoSyncing {
    static final protected TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;
    static final Integer rpcWaitTimeoutSeconds = 10*60*60  // Wait up to 10 (!) hours for RPC response
    private static OmniClient INSTANCE;

    @Delegate
    OmniClient client() {
        if (INSTANCE == null) {
            INSTANCE = new OmniClient(BitcoinNetwork.MAINNET, RpcURI.defaultMainNetURI, rpcTestUser, rpcTestPassword)
        }
        return INSTANCE;
    }

    OmniClient getClient() {
        return client()
    }

    /**
     * Wait for RPC server to be responding and to be in sync with the Bitcoin Blockchain
     */
    void setupSpec() {
        Boolean available = client().waitForServer(rpcWaitTimeoutSeconds)
        if (!available) {
            log.error "Timeout error."
        }
        assert available

        //
        // Get in sync with the Blockchain
        //
        waitForSync(client);

        var info = client().getNetworkInfo()
        var omniInfo = client().omniGetInfo()
        var omniVersion = omniInfo.omnicoreversion
        log.info "Bitcoin version: ${info.version}"
        log.info "Omni Core version: ${omniVersion}"
    }
}
