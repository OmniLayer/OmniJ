package org.mastercoin.test.rpc

import org.mastercoin.BaseRegTestSpec
import org.mastercoin.CurrencyID
import spock.lang.Unroll

import static org.mastercoin.CurrencyID.MSC
import static org.mastercoin.CurrencyID.TMSC


/**
 * Specification for the traditional distributed exchange
 */
class MSCDexSpec extends BaseRegTestSpec {

    final static BigDecimal stdCommitFee = 0.0001
    final static BigInteger stdBlockSpan = 10

    def "The generated hex-encoded transaction matches a valid reference transaction" () {
        given:
        def txHex = createDexSellOfferHex(MSC, 1.0, 0.2, stdBlockSpan, stdCommitFee, 1)
        def expectedHex = "00010014000000010000000005f5e1000000000001312d000a000000000000271001"

        expect:
        txHex == expectedHex
    }

    def "A new sell offer can be created with Action = 1 (New)"() {
        given:
        def startBTC = 0.1
        def startMSC = 2.5
        def currencyOffered = MSC
        def amountOffered = 1.0
        def desiredBTC = 0.2
        def action = 1
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def activeOffersAtTheStart = getactivedexsells_MP()

        when: "creating an offer with action = 1"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, action)
        generateBlock()

        then: "it is a valid transaction"
        def offerTx = getTransactionMP(offerTxid)
        offerTx.confirmations == 1
        offerTx.valid == true

        and: "a new offer is created on the distributed exchange"
        def activeOffersNow = getactivedexsells_MP()
        activeOffersNow.size() == activeOffersAtTheStart.size() + 1
    }

    @Unroll
    def "An accepted currency identifier for sell offers is #currencyOffered"() {
        given:
        def startBTC = 0.1
        def startMSC = 2.5
        def amountOffered = 1.0
        def desiredBTC = 0.2
        def action = 1
        def fundedAddress = createFundedAddress(startBTC, startMSC)

        when: "an offer of #currencyId is created"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, action)
        generateBlock()

        then: "the transaction should be a valid offering of #currencyOffered"
        def offerTx = getTransactionMP(offerTxid)
        offerTx.valid == true
        offerTx.propertyid == currencyOffered

        where: "the currency identifier is either MSC or TMSC"
        currencyOffered << [new CurrencyID(1), new CurrencyID(2)]
    }

    def "Offering more tokens than available puts up an offer with the available amount"() {
        given:
        def startBTC = 0.1
        def startMSC = 2.5
        def currencyOffered = MSC
        def desiredBTC = 50.0
        def action = 1
        def fundedAddress = createFundedAddress(startBTC, startMSC)

        when: "the amount offered for sale exceeds the sending address's available balance"
        def amountAvailableAtStart = getbalance_MP(fundedAddress, currencyOffered).balance
        def amountOffered = amountAvailableAtStart + 100.0

        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, action)
        generateBlock()

        then: "this indicates to sell all tokens that are available"
        def offerTx = getTransactionMP(offerTxid)
        def offerAmount = new BigDecimal(offerTx.amount)
        def amountAvailableNow = getbalance_MP(fundedAddress, currencyOffered).balance

        offerTx.valid == true
        offerAmount == amountAvailableAtStart
        offerAmount == Math.min(amountAvailableAtStart, amountOffered)
        amountAvailableNow == 0.0
    }

    def "The amount offered for sale is reserved from the available balance"() {
        given:
        def startBTC = 0.1
        def startMSC = 100.0
        def currencyOffered = MSC
        def amountOffered = 90.0
        def desiredBTC = 45.0
        def action = 1
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = getbalance_MP(fundedAddress, currencyOffered)

        when: "an amount is offered for sale"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, action)
        generateBlock()

        then: "the offered amount is reserved and subtracted from the available balance"
        def offerTx = getTransactionMP(offerTxid)
        def offerAmount = new BigDecimal(offerTx.amount)
        def balanceNow = getbalance_MP(fundedAddress, currencyOffered)

        offerTx.valid == true
        balanceNow.balance == balanceAtStart.balance - offerAmount
        balanceNow.reserved == balanceAtStart.reserved + offerAmount
    }

    def "Receiving tokens doesn't increase the offered amount of a published offer"() {
        given:
        def startBTC = 0.1
        def startMSC  = 2.5
        def currencyOffered = MSC
        def offerMSC = 90.0
        def desiredBTC = 45.0
        def action = 1
        def startOtherMSC = 10.0
        def additionalMSC = 10.0
        def fundedAddress = createFundedAddress(startBTC, startMSC)

        when: "the sell offer is published"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, offerMSC, desiredBTC, stdBlockSpan, stdCommitFee, action)
        generateBlock()

        and: "additional tokens are received"
        def offerBeforeReceivingMore = getTransactionMP(offerTxid)
        def balanceBeforeReceivingMore = getbalance_MP(fundedAddress, currencyOffered)
        def otherAddress = createFundedAddress(startBTC, startOtherMSC)
        def sendTxid = send_MP(otherAddress, fundedAddress, currencyOffered, additionalMSC)
        generateBlock()

        then: "any tokens received are added to the available balance"
        def balanceNow = getbalance_MP(fundedAddress, currencyOffered)
        def sendTx = getTransactionMP(sendTxid)
        def sendAmount = new BigDecimal(sendTx.amount)
        sendTx.valid == true
        balanceNow.balance == balanceBeforeReceivingMore.balance + sendAmount

        and: "are not included in the amount for sale by this sell offer"
        def offerNow = getTransactionMP(offerTxid)
        offerNow.valid == true
        offerNow.amount == offerBeforeReceivingMore.amount
        balanceNow.reserved == balanceBeforeReceivingMore.reserved
    }

    def "There can be only one active offer that accepts BTC"() {
        given:
        def startBTC = 0.1
        def startMSC  = 2.5
        def currencyOffered = MSC
        def firstOfferMSC = 1.0
        def firstOfferBTC = 0.2
        def secondOfferMSC = 1.5
        def secondOfferBTC = 0.3
        def action = 1
        def fundedAddress = createFundedAddress(startBTC, startMSC)

        when: "there is already an active offer accepting BTC"
        def firstOfferTxid = createDexSellOffer(
                fundedAddress, currencyOffered, firstOfferMSC, firstOfferBTC, stdBlockSpan, stdCommitFee, action)
        generateBlock()

        then: "creating any other offer accepting BTC is rejected"
        def secondOfferTxid = createDexSellOffer(
                fundedAddress, currencyOffered, secondOfferMSC, secondOfferBTC, stdBlockSpan, stdCommitFee, action)
        generateBlock()

        getTransactionMP(firstOfferTxid).valid == true
        getTransactionMP(secondOfferTxid).valid == false
    }

}
