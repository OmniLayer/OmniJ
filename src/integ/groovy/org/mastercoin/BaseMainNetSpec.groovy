package org.mastercoin

import com.msgilligan.bitcoin.rpc.RPCURL
import org.mastercoin.rpc.MastercoinCLIClient
import groovy.json.JsonSlurper
import org.mastercoin.rpc.MastercoinClientDelegate
import spock.lang.Specification

/**
 * Base specification for tests on Main net
 *
 * Creates an RPC client (currently <code>MastercoinCLIClient</code>), waits for
 * RPC server to be responding (typical integration/functional requests require starting
 * an RPC server which can take minutes or even hours) and to be in sync with the main
 * Bitcoin Blockchain.
 *
 */
abstract class BaseMainNetSpec extends Specification implements MastercoinClientDelegate {
    {
        client = new MastercoinCLIClient(RPCURL.defaultMainNetURL, BaseMainNetSpec.rpcuser, BaseMainNetSpec.rpcpassword)
    }
    static final String rpcuser = "bitcoinrpc"
    static final String rpcpassword = "pass"
    static final Integer rpcWaitTimeoutSeconds = 3*60*60  // Wait up to 3 hours for RPC response

    /**
     * Wait for RPC server to be responding and to be in sync with the Bitcoin Blockchain
     */
    void setupSpec() {
        println "Waiting for server..."
        Boolean available = client.waitForServer(rpcWaitTimeoutSeconds)
        if (!available) {
            println "Timeout error."
        }

        //
        // Get in sync with the Blockchain
        //
        def curHeight = 0
        def newHeight = getReferenceBlockHeight()
        println "Blockchain.info current height: ${newHeight}"
        while ( newHeight > curHeight ) {
            curHeight = newHeight
            Boolean upToDate = client.waitForSync(curHeight, 60*60)
            newHeight = getReferenceBlockHeight()
            println "Current reference block height: ${newHeight}"
        }
    }

    /**
     * Use an external service to get the current block height
     * Currently uses blockchain.info
     */
    def getReferenceBlockHeight() {
        // Use Blockchain.info to get the current block height
        def height = new JsonSlurper().parse(new URL("http://blockchain.info/latestblock")).height
        return height
    }
}
