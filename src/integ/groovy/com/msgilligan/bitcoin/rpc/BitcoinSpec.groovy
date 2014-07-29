package com.msgilligan.bitcoin.rpc

import org.mastercoin.BaseRegTestSpec

import java.lang.Void as Should

class BitcoinSpec extends BaseRegTestSpec {
    static final BigDecimal testAmount = 2.0

    Should "return basic info" () {
        when: "we request info"
            def info = getInfo()

        then: "we get back some basic information"
            info != null
            info.version >= 90100
            info.protocolversion >= 70002
    }

    Should "Generate a block upon request"() {
        given: "a certain starting count"
        def startCount = client.blockCount

        when: "we generate 1 new block"
        generateBlocks(1)

        then: "the block count is 1 higher"
        blockCount == startCount + 1

    }

    Should "Send an amount to a newly created address"() {
        when: "we create a new address and send testAmount to it"
        def destinationAddress = getNewAddress()
        sendToAddress(destinationAddress, testAmount, "comment", "comment-to")
        generateBlocks(1)

        then: "the new address has a balance of testAmount"
        testAmount == getReceivedByAddress(destinationAddress)
        // TODO: check balance of source address/wallet
    }

    Should "Get a list of unspent Transactions"() {
        when: "we request unspent Transactions"
        def unspent = listUnspent()

        then: "there is at least 1"
        unspent.size() >= 1
    }
}
