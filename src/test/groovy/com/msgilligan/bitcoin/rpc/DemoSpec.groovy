package com.msgilligan.bitcoin.rpc

/**
 * User: sean
 * Date: 6/16/14
 * Time: 12:30 PM
 */
class DemoSpec extends BaseRPCSpec {

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
