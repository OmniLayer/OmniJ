package com.msgilligan.bitcoinj.rpc

import groovy.json.JsonSlurper

/**
 * Implementation of BlockchainSyncing that uses Blockchain.info API
 */
trait BlockchainDotInfoSyncing extends BlockchainSyncing {

    Integer getReferenceBlockHeight() {
        Integer height = new JsonSlurper().parse(new URL("https://blockchain.info/latestblock")).height
        return height
    }
}