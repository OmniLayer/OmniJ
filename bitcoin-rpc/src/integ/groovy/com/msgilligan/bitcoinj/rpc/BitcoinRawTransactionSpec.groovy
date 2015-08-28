package com.msgilligan.bitcoinj.rpc

import com.msgilligan.bitcoinj.BaseRegTestSpec
import org.bitcoinj.core.Address
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Tests of creating and sending raw transactions via RPC
 */
@Stepwise
class BitcoinRawTransactionSpec extends BaseRegTestSpec {
    final static BigDecimal fundingAmount = 10.0
    final static BigDecimal sendingAmount = 1.0

    @Shared
    Address fundingAddress

    @Shared
    Address destinationAddress

    @Shared
    String rawTransactionHex

    def "Fund address as intermediate"() {
        when: "a new address is created"
        fundingAddress = getNewAddress()

        and: "coins are sent to the new address from a random source"
        sendToAddress(fundingAddress, fundingAmount)

        and: "a new block is mined"
        generateBlock()

        then: "the address should have that balance"
        def balance = getBitcoinBalance(fundingAddress)
        balance == fundingAmount
    }

    def "Create unsigned raw transaction"() {
        given: "a newly created address as destination"
        destinationAddress = getNewAddress("destinationAddress")

        when: "we create a transaction, spending from #fundingAddress to #destinationAddress"
        rawTransactionHex = createRawTransaction(fundingAddress, destinationAddress, sendingAmount)

        then: "there should be a raw transaction"
        rawTransactionHex != null
        rawTransactionHex.size() > 0
    }

    def "Sign unsigned raw transaction"() {
        when: "the transaction is signed"
        def result = signRawTransaction(rawTransactionHex)
        rawTransactionHex = result["hex"]

        then: "all inputs should be signed"
        result["complete"] == true
    }

    def "Broadcast signed raw transaction"() {
        when: "the transaction is sent"
        def txid = sendRawTransaction(rawTransactionHex)

        then: "there should be a transaction hash"
        txid != null

        when: "a new block is mined"
        generateBlock()

        then: "the transaction should have 1 confirmation"
        def broadcastedTransaction = getRawTransaction(txid, true)
        def confirmations = broadcastedTransaction["confirmations"]
        confirmations == 1

        and: "#fundingAddress has a remainder of coins minus transaction fees"
        def balanceRemaining = getBitcoinBalance(fundingAddress)
        balanceRemaining == fundingAmount - sendingAmount - stdTxFee

        and: "#destinationAddress has a balance matching the spent amount"
        def balanceDestination = getBitcoinBalance(destinationAddress)
        balanceDestination == sendingAmount
    }

    def "Send Bitcoin"() {
        when: "a new address is created"
        def newAddress = getNewAddress()

        and: "coins are sent to the new address from #destinationAddress"
        def amount = sendingAmount - stdTxFee
        sendBitcoin(destinationAddress, newAddress, amount)

        and: "a new block is mined"
        generateBlock()

        then: "the sending address should be empty"
        def balanceSource = getBitcoinBalance(destinationAddress)
        balanceSource == 0

        and: "the new adress should have the amount sent to"
        def balance = getBitcoinBalance(newAddress)
        balance == amount
    }
}
