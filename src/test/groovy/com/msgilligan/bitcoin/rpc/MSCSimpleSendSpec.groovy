package com.msgilligan.bitcoin.rpc

import spock.lang.Shared

/**
 * User: sean
 * Date: 6/17/14
 * Time: 4:11 PM
 */
class MSCSimpleSendSpec extends BaseRPCSpec {
    @Shared
    Long currencyMSC = 1L
    // Need to make sure these variables are set up with values that match their names
    Long nonExistantCurrencyID = 293487L
    def emptyAddress = client.getNewAddress()
    def addressWith1MSC = client.getNewAddress() // Get an address with a balance of 1 MSC
    def richAddress = client.getNewAddress()  // Should be an address we know has a > 0 balance,
                                              //      ... otherwise it will fail
                                              // We need to seed the address with coins

//    def "Can generate 255365 blocks"() {
//        when: "we generate 255365 blocks"
//            def startCount = client.getBlockCount()         // Get starting block count
//            client.setGenerate(true, 255365)                // Generate blocks
//
//        then: "the block count is 255365 higher"
//            client.getBlockCount() == startCount+ 255365                // Verify block count
//    }

    def "Can simple send MSC from one address to another" () {

        when: "we send MSC"
            def richBalance = client.getMPbalance(richAddress, currencyMSC)
            def amount = 1.0
            def toAddress = client.getNewAddress()      // New address
            client.sendMPsimple(richAddress, toAddress, currencyMSC, amount)

        and: "a block is generated"
            client.setGenerate(true, 1)                // Generate 1 block
            def newRichBalance = client.getMPbalance(richAddress, currencyMSC)

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
            amount == client.getbalance_MP(toAddress, currencyMSC)
            newRichBalance == richBalance - amount
    }

    def "Invalid Simple Sends defined in spec are rejected by the RPC"() {
        // Note: We also need to submit via P2P and confirm these same invalid tx'es and make sure they are
        // treated as invalid by the Master Core parser

        given: "a new, empty destination address"
            def toAddress = client.getNewAddress()

        when: "the amount to transfer is zero"
            client.sendMPsimple(richAddress, toAddress, currencyMSC, 0)
        // TODO: Test sending a negative amount of coins?


        // TODO: Check that the *right type* of exceptions are thrown
        // Currently it seems they're all 500s
        then: "exception is thrown"
            Exception e1 = thrown()
        // TODO: Verify that blockchain state didn't change

        when: "the sending address has zero coins in its available balance for the specified currency identifier"
            client.send_MP(emptyAddress, toAddress, currencyMSC, 1.0)

        then: "exception is thrown"
            Exception e2 = thrown()

        when: "the amount to transfer exceeds the number owned and available by the sending address"
            client.send_MP(addressWith1MSC, toAddress, currencyMSC, 1.00000001)

        then: "exception is thrown"
            Exception e3 = thrown()

        when: "the specified currency identifier is non-existent"
            client.send_MP(richAddress, toAddress, nonExistantCurrencyID, 1.0)

        then: "exception is thrown"
            Exception e4 = thrown()
    }

//    def "TODO: test currencies other than MSC - when implemented"() {}
//    def "TODO: test savings address -- when implemented"() {}
//    def "TODO: test invalid transaction version -- raw"() {}
//    def "TODO: test invalid transaction type - raw"() {}
//    def "TODO: generate other invalid/edge transactions and submit via P2P and/or confirm in a bock -- raw"() {}
//    def "TODO: double spend -- raw"() {}

}
