package com.msgilligan.bitcoin.rpc

/**
 * Wait for synchronization with a reference source of block height.
 *
 * Since synchronization may take time, we check the block height again
 * after waitForBlock returns.
 */
trait BlockchainSyncing {

    def waitForSync(BitcoinClient client) {
        //
        // Get in sync with the block chain
        //
        def curHeight = 0
        def newHeight = getReferenceBlockHeight()
        println "Blockchain.info current height: ${newHeight}"
        while ( newHeight > curHeight ) {
            curHeight = newHeight
            Boolean upToDate = client.waitForBlock(curHeight, 60*60)
            newHeight = getReferenceBlockHeight()
            println "Current reference block height: ${newHeight}"
        }

    }

    /**
     * Use an external reference to get the current block height
     */
    abstract Integer getReferenceBlockHeight()
}