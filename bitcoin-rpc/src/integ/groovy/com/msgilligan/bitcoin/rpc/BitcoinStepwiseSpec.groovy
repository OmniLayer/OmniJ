package com.msgilligan.bitcoin.rpc

import com.msgilligan.bitcoin.BaseRegTestSpec
import org.bitcoinj.core.Address
import spock.lang.Shared

class BitcoinStepwiseSpec extends BaseRegTestSpec {
    final static BigDecimal sendAmount = 10.0
    final static BigDecimal extraAmount = 0.10

    @Shared
    Address wealthyAddress

    def "Be able to fund wealthy account from mining profits"() {
        when: "we send some BTC to an address"
        wealthyAddress = getNewAddress()
        sendToAddress(wealthyAddress, 2*sendAmount + extraAmount)
        generateBlock()

        then: "we have the correct amount of BTC there"
        getBitcoinBalance(wealthyAddress) == 2*sendAmount + extraAmount
    }

    def "Send an amount to a newly created address"() {
        setup: "initial balance"
        def wealthyStartBalance = getBitcoinBalance(wealthyAddress)
        def testAmount = 1.0

        when: "we create a new address and send testAmount to it"
        Address destinationAddress = getNewAddress()
        sendBitcoin(wealthyAddress, destinationAddress, testAmount)
        generateBlock()

        then: "the new address has a balance of testAmount"
        getBitcoinBalance(destinationAddress) == testAmount

        and: "the source address is poorer by the correct amount"
        getBitcoinBalance(wealthyAddress) == wealthyStartBalance - testAmount - stdTxFee
    }

}