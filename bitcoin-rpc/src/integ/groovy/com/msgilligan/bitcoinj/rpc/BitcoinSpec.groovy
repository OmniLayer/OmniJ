package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.BaseRegTestSpec
import org.bitcoinj.params.RegTestParams

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

    def "Get a list of available commands"() {
        given:
        def commands = getCommands()

        expect:
        commands != null
        commands.contains('getinfo')
        commands.contains('help')
        commands.contains('stop')
    }

    def "Use RegTest mode to generate a block upon request"() {
        given: "a certain starting height"
        def startHeight = blockCount

        when: "we generate 1 new block"
        generateBlock()

        then: "the block height is 1 higher"
        blockCount == startHeight + 1
    }

    def "When we send an amount to a newly created address, it arrives"() {
        given: "A new, empty Bitcoin address"
        def destinationAddress = getNewAddress()

        when: "we send it testAmount (from coins mined in RegTest mode)"
        sendToAddress(destinationAddress, testAmount, "comment", "comment-to")

        and: "we generate 1 new block"
        generateBlock()

        then: "the new address has a balance of testAmount"
        testAmount == getReceivedByAddress(destinationAddress)
        // TODO: check balance of source address/wallet
    }

    def "Get a list of unspent transaction outputs"() {
        when: "we request unspent transaction outputs"
        def unspent = listUnspent()

        then: "there is at least 1"
        unspent.size() >= 1
    }

    def "Get a filtered list of unconfirmed transaction outputs"() {
        when: "we create a new address and send #testAmount to it"
        def destinationAddress = getNewAddress()
        sendToAddress(destinationAddress, testAmount, "comment", "comment-to")

        and: "we request unconfirmed unspent outputs for #destinationAddress"
        def unspent = listUnspent(0, 0, [destinationAddress])

        then: "there is at least 1"
        unspent.size() >= 1

        and: "they have 0 confirmations"
        unspent.every { output -> output.confirmations == 0 }

        and: "they are associated with #destinationAddress"
        unspent.every { output -> output.address == destinationAddress }
    }

    def "We can get the correct private key for an address"() {
        when: "we create a new address and dump it's private key"
        def address = getNewAddress()
        def netParams = RegTestParams.get()
        def key = dumpPrivKey(address)

        then: "when we convert the dumped key to an address we get the same address"
        key.toAddress(netParams) == address
    }
}
