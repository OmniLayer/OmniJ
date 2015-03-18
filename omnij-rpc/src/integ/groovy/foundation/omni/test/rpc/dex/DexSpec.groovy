package foundation.omni.test.rpc.dex

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import spock.lang.Unroll

import static CurrencyID.MSC

/**
 * Specification for the traditional distributed exchange
 */
class DexSpec extends BaseRegTestSpec {

    final static BigDecimal stdCommitFee = 0.0001
    final static BigInteger stdBlockSpan = 10
    final static Short actionNew = 1

    def "The generated hex-encoded transaction matches a valid reference transaction"() {
        given:
        def txHex = createDexSellOfferHex(MSC, 1.0, 0.2, 10, 0.0001, 1)
        def expectedHex = "00010014000000010000000005f5e1000000000001312d000a000000000000271001"

        expect:
        txHex == expectedHex
    }

    def "A new sell offer can be created with Action = 1 (New)"() {
        given:
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

        where:
        [startBTC, startMSC, currencyOffered, amountOffered, desiredBTC] << [[0.1, 2.5, MSC, 1.0, 0.2]]
    }

    @Unroll
    def "An accepted currency identifier for sell offers is #currencyOffered"() {
        given:
        def startBTC = 0.1
        def startMSC = 2.5
        def amountOffered = 1.0
        def desiredBTC = 0.2
        def fundedAddress = createFundedAddress(startBTC, startMSC)

        when: "an offer of #currencyId is created"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
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
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def amountAvailableAtStart = getbalance_MP(fundedAddress, currencyOffered).balance
        def amountOffered = amountAvailableAtStart + 100.0

        when: "the amount offered for sale exceeds the sending address's available balance"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        then: "this indicates to sell all tokens that are available"
        def offerTx = getTransactionMP(offerTxid)
        def offerAmount = new BigDecimal(offerTx.amount)
        def amountAvailableNow = getbalance_MP(fundedAddress, currencyOffered).balance
        offerTx.valid == true
        offerAmount == amountAvailableAtStart
        offerAmount == Math.min(amountAvailableAtStart, amountOffered)
        amountAvailableNow == 0.0

        where:
        [startBTC, startMSC, currencyOffered, desiredBTC] << [[0.1, 2.5, MSC, 50.0]]
    }

    def "The amount offered for sale is reserved from the available balance"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = getbalance_MP(fundedAddress, currencyOffered)

        when: "an amount is offered for sale"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        then: "the offered amount is reserved and subtracted from the available balance"
        def offerTx = getTransactionMP(offerTxid)
        def offerAmount = new BigDecimal(offerTx.amount)
        def balanceNow = getbalance_MP(fundedAddress, currencyOffered)
        offerTx.valid == true
        balanceNow.balance == balanceAtStart.balance - offerAmount
        balanceNow.reserved == balanceAtStart.reserved + offerAmount

        where:
        [startBTC, startMSC, currencyOffered, amountOffered, desiredBTC] << [[0.1, 100.0, MSC, 90.0, 45.0]]
    }

    def "Receiving tokens doesn't increase the offered amount of a published offer"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)

        when: "the sell offer is published"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, offerMSC, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
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

        where:
        [startBTC, startMSC, currencyOffered, offerMSC, desiredBTC,
         startOtherMSC, additionalMSC] << [[0.1, 2.5, MSC, 90.0, 45.0, 10.0, 10.0]]
    }

    def "There can be only one active offer that accepts BTC"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)

        when: "there is already an active offer accepting BTC"
        def firstOfferTxid = createDexSellOffer(
                fundedAddress, currencyOffered, firstOfferMSC, firstOfferBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        and: "and another offer accepting BTC is made"
        def secondOfferTxid = createDexSellOffer(
                fundedAddress, currencyOffered, secondOfferMSC, secondOfferBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        then: "the other offer is rejected"
        getTransactionMP(firstOfferTxid).valid == true
        getTransactionMP(secondOfferTxid).valid == false

        where:
        [startBTC, startMSC, currencyOffered, firstOfferMSC, firstOfferBTC,
         secondOfferMSC, secondOfferBTC] << [[0.1, 2.5, MSC, 1.0, 0.2, 1.5, 0.3]]
    }

}
