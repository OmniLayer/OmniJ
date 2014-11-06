package com.msgilligan.bitcoin.rpc

import org.mastercoin.BaseRegTestSpec
import spock.lang.Shared

import java.lang.Void as Should
import java.security.SecureRandom

class BitcoinStepwiseSpec extends BaseRegTestSpec {
    final static BigDecimal sendAmount = 10.0
    final static BigDecimal extraAmount = 0.10

    @Shared
    def accountname

    @Shared
    def wealthyAddress

    def setupSpec() {
        // Create a new, unique address in a dedicated account
        def random = new SecureRandom();
        accountname = "msc" + new BigInteger(130, random).toString(32)
        wealthyAddress = getAccountAddress(accountname)
    }


    void "Be able to fund wealthy account from mining profits"() {
        when: "we create a new account for Mastercoins and send some BTC to it"
        sendToAddress(wealthyAddress, 2*sendAmount + extraAmount)
        generateBlock()

        then: "we have the correct amount of BTC there"
        getBalance(accountname) >= 2*sendAmount + extraAmount
    }

    void "Send an amount to a newly created address"() {
        setup: "initial balance"
        def wealthyStartBalance = client.getBalance(accountname)
        def testAmount = 1.0

        when: "we create a new address and send testAmount to it"
        def destinationAddress = getNewAddress()
        sendFrom(accountname, destinationAddress, testAmount)
        generateBlocks(1)

        then: "the new address has a balance of testAmount"
        getReceivedByAddress(destinationAddress) == testAmount

        and: "the source address is poorer"
        getBalance(accountname) <= wealthyStartBalance - testAmount
    }

}