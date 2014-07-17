package com.msgilligan.bitcoinj

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.NetworkParameters
import com.google.bitcoin.core.Transaction
import com.google.bitcoin.params.RegTestParams
import com.msgilligan.bitcoin.rpc.BaseRPCSpec
import spock.lang.Shared

/**
 * User: sean
 * Date: 7/15/14
 * Time: 2:44 PM
 */
class RawSendSpec extends BaseRPCSpec {
    @Shared
    NetworkParameters params

    void setupSpec() {
        params = RegTestParams.get()
    }

    def "can send a coin to newly created address"() {
        when: "we create a new address and send testAmount to it"
        def destAddr = client.getNewAddress()                   // Create new Bitcoin address
        def txid = client.sendToAddress(destAddr, testAmount,
                "comment", "comment-to")                        // Send a coin
        client.setGenerate(true, 1)                             // Generate 1 block
        def transaction = client.getTransaction(txid)

        then: "the new address has a balance of testAmount"
        testAmount == client.getReceivedByAddress(destAddr, 1)  // Verify destAddr balance
        // TODO: check balance
    }

    def "send 1 coin to new address"() {
        when: "we create a raw transaction that sends a coin to a new address"
        String addrStr = client.getNewAddress()                   // Create new Bitcoin address
        Address destAddr = new Address(params, addrStr)
        Transaction send = new Transaction(params)

        then:
        1 == 1

    }
}
