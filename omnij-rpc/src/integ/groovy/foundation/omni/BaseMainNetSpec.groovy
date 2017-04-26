package foundation.omni

import com.msgilligan.bitcoinj.rpc.BlockchainDotInfoSyncing
import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException
import com.msgilligan.bitcoinj.rpc.Loggable
import com.msgilligan.bitcoinj.rpc.RPCURI
import foundation.omni.rpc.OmniCLIClient
import foundation.omni.rpc.OmniClientDelegate
import foundation.omni.rpc.test.TestServers
import org.bitcoinj.params.MainNetParams
import spock.lang.Specification

/**
 * Base specification for integration tests on Main net
 *
 * Creates an RPC client (currently <code>OmniCLIClient</code>), waits for
 * RPC server to be responding (typical integration/functional requests require starting
 * an RPC server which can take minutes or even hours) and to be in sync with the main
 * Bitcoin Blockchain.
 *
 */
abstract class BaseMainNetSpec extends Specification implements OmniClientDelegate,
        BlockchainDotInfoSyncing, Loggable {
    static final protected TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;
    {
        client = new OmniCLIClient(MainNetParams.get(), RPCURI.defaultMainNetURI, rpcTestUser, rpcTestPassword)
    }
    static final Integer rpcWaitTimeoutSeconds = 10*60*60  // Wait up to 10 (!) hours for RPC response

    /**
     * Wait for RPC server to be responding and to be in sync with the Bitcoin Blockchain
     */
    void setupSpec() {
        Boolean available = client.waitForServer(rpcWaitTimeoutSeconds)
        if (!available) {
            log.error "Timeout error."
        }
        assert available

        //
        // Get in sync with the Blockchain
        //
        waitForSync(client);

        def info = client.getinfo()

        def omniVersion
        def infoMP
        try {
            infoMP = client.omniGetInfo()
        } catch (JsonRPCStatusException e) {
            log.error "Exception calling omniGetInfo()"
        }
        if (infoMP?.mastercoreversion) {
            omniVersion = infoMP.mastercoreversion.toString()
        } else {
            omniVersion = info.mastercoreversion
        }
        log.info "Bitcoin version: ${info.version}"
        log.info "Omni Core version: ${omniVersion}"
    }
}
