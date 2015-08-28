package com.msgilligan.bitcoinj.rpc

/**
 * Wait for synchronization with a reference source of block height.
 *
 * Since synchronization may take time, we check the block height again
 * after waitForBlock returns.
 */
trait BlockchainSyncing extends Loggable {

    def waitForSync(BitcoinClient client) {
        //
        // Get in sync with the block chain
        //
        def curHeight = 0
        def newHeight = getReferenceBlockHeight()
        log.info "Reference current height: {}", newHeight
        while ( newHeight > curHeight ) {
            curHeight = newHeight
            Boolean upToDate = client.waitForBlock(curHeight, 60*60)
            newHeight = getReferenceBlockHeight()
            log.info "Current reference block height: {}", newHeight
        }

    }

    /**
     * Use an external reference to get the current block height
     * See: BlockchainDotInfoSyncing
     */
    abstract Integer getReferenceBlockHeight()
}