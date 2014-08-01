package com.msgilligan.bitcoin.rpc

import org.mastercoin.BaseRegTestSpec

class BitcoinSpec extends BaseRegTestSpec {
    static final BigDecimal testAmount = 2.0

    def "return basic info" () {
        when: "we request info"
        def info = getInfo()

        then: "we get back some basic information"
        info != null
        info.version >= 90100
        info.protocolversion >= 70002
    }

    def "Generate a block upon request"() {
        given: "a certain starting height"
        def startHeight = blockCount

        when: "we generate 1 new block"
        generateBlocks(1)

        then: "the block height is 1 higher"
        blockCount == startHeight + 1

    }

    def "Send an amount to a newly created address"() {
        when: "we create a new address and send testAmount to it"
        def destinationAddress = getNewAddress()
        sendToAddress(destinationAddress, testAmount, "comment", "comment-to")
        generateBlocks(1)

        then: "the new address has a balance of testAmount"
        testAmount == getReceivedByAddress(destinationAddress)
        // TODO: check balance of source address/wallet
    }

    def "Get a list of unspent Transactions"() {
        when: "we request unspent Transactions"
        def unspent = listUnspent()

        then: "there is at least 1"
        unspent.size() >= 1
    }
}
