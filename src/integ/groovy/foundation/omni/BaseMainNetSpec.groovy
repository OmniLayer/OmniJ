package foundation.omni

import com.msgilligan.bitcoin.rpc.JsonRPCStatusException
import com.msgilligan.bitcoin.rpc.RPCURI
import foundation.omni.consensus.WaitForBlockchainSync
import foundation.omni.rpc.OmniCLIClient
import foundation.omni.rpc.OmniClientDelegate
import groovy.util.logging.Slf4j
import spock.lang.Specification

/**
 * Base specification for integration tests on Main net
 *
 * Creates an RPC client (currently <code>MastercoinCLIClient</code>), waits for
 * RPC server to be responding (typical integration/functional requests require starting
 * an RPC server which can take minutes or even hours) and to be in sync with the main
 * Bitcoin Blockchain.
 *
 */
@Slf4j
abstract class BaseMainNetSpec extends Specification implements OmniClientDelegate {
    {
        client = new OmniCLIClient(RPCURI.defaultMainNetURI, BaseMainNetSpec.rpcuser, BaseMainNetSpec.rpcpassword)
    }
    static final String rpcuser = "bitcoinrpc"
    static final String rpcpassword = "pass"
    static final Integer rpcWaitTimeoutSeconds = 3*60*60  // Wait up to 3 hours for RPC response

    /**
     * Wait for RPC server to be responding and to be in sync with the Bitcoin Blockchain
     */
    void setupSpec() {
        log.info "Waiting for server..."
        Boolean available = client.waitForServer(rpcWaitTimeoutSeconds)
        if (!available) {
            log.error "Timeout error."
        }
        assert available

        //
        // Get in sync with the Blockchain
        //
        WaitForBlockchainSync.waitForSync(client);

        def info = client.getinfo()

        def mscVersion
        def infoMP
        try {
            infoMP = client.getinfo_MP()
        } catch (JsonRPCStatusException e) {
            /* swallow */
        }
        if (infoMP?.mastercoreversion) {
            mscVersion = infoMP.mastercoreversion.toString()
        } else {
            mscVersion = info.mastercoreversion
        }
        log.info "Bitcoin version: ${info.version}"
        log.info "Mastercore version: ${mscVersion}"
    }
}
