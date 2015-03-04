package foundation.omni.consensus

import com.msgilligan.bitcoin.rpc.BitcoinClient
import groovy.json.JsonSlurper

/**
 * Utility class that uses a reference (currently Blockchain.info API) and waits for an BitcoinClient
 * to reach the current blockheight.
 *
 * There is a waitForSync method in BitcoinClient that should be used instead.
 */
@Deprecated()
class WaitForBlockchainSync {

    static def waitForSync(BitcoinClient client) {
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
    static def getReferenceBlockHeight() {
        // Use Blockchain.info to get the current block height
        def height = new JsonSlurper().parse(new URL("https://blockchain.info/latestblock")).height
        return height
    }

}
