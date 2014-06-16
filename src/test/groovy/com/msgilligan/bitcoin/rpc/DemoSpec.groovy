package com.msgilligan.bitcoin.rpc

import spock.lang.Shared
import spock.lang.Specification

import static BitcoinClient.SATOSHIS_PER_BITCOIN

/**
 * User: sean
 * Date: 6/16/14
 * Time: 12:30 PM
 */
class DemoSpec extends Specification {
    static def rpcproto = "http"
    static def rpchost = "127.0.0.1"
    static def rpcport = 28332
    static def rpcfile = "/"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    static BigDecimal minSatoshisForTest = 5.0
    static BigDecimal testAmount = 1.0

    @Shared
    BitcoinClient client;

    void setupSpec() {
        // Instantiate a Bitcoin RPC Client
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new BitcoinClient(rpcServerURL, rpcuser, rpcpassword)

        // Make sure we have enough test coins
        def balance = client.getBalance(null, null);
        if (balance > minSatoshisForTest) {
            client.setGenerate(true, 101)
        }
    }

    def "can write a block"() {
        when: "we generate 1 new block"
            def startCount = client.getBlockCount()                 // Get starting block count
            client.setGenerate(true, 1)                             // Generate 1 block

        then: "the block count is 1 higher"
            client.getBlockCount() == startCount + 1                // Verify block count

    }

    def "can send a coin to newly created address"() {
        when: "we create a new address and send testAmount to it"
            def destAddr = client.getNewAddress()                   // Create new Bitcoin address
            client.sendToAddress(destAddr, testAmount,
                    "comment", "comment-to")                        // Send a coin
            client.setGenerate(true, 1)                             // Generate 1 block

        then: "the new address has a balance of testAmount"
            testAmount == client.getReceivedByAddress(destAddr, 1)  // Verify destAddr balance
    }
}
