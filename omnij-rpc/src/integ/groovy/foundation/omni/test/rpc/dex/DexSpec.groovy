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
    final static Byte stdBlockSpan = 10
    final static Byte actionNew = 1
    final static Byte actionUpdate = 2
    final static Byte actionCancel = 3

    def "A new sell offer can be created with Action = 1 (New)"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)
        def activeOffersAtTheStart = getactivedexsells_MP()

        when: "creating an offer with action = 1"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
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
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)

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
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)

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
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)

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

    def "An offer can be updated with action = 2, and cancelled with action = 3"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = getbalance_MP(fundedAddress, currencyOffered)
        def offersAtStart = getactivedexsells_MP()

        when: "creating an offer with action 1"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, offeredMSC, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        then:
        getTransactionMP(offerTxid).valid
        getTransactionMP(offerTxid).amount as BigDecimal == offeredMSC

        and:
        getbalance_MP(fundedAddress, currencyOffered).balance == balanceAtStart.balance - offeredMSC
        getbalance_MP(fundedAddress, currencyOffered).reserved == balanceAtStart.reserved + offeredMSC

        and: "a new offer is listed"
        getactivedexsells_MP().size() == offersAtStart.size() + 1

        when: "updating an offer with action = 2"
        def updateTxid = createDexSellOffer(
                fundedAddress, currencyOffered, updatedMSC, updatedBTC, stdBlockSpan, stdCommitFee, actionUpdate)
        generateBlock()

        then: "the offered amount is updated"
        getTransactionMP(updateTxid).valid
        getTransactionMP(updateTxid).amount as BigDecimal == updatedMSC

        and: "the total amount offered is reserved"
        getbalance_MP(fundedAddress, currencyOffered).balance == balanceAtStart.balance - updatedMSC
        getbalance_MP(fundedAddress, currencyOffered).reserved == balanceAtStart.reserved + updatedMSC

        when: "cancelling an offer with action = 3"
        def cancelTxid = createDexSellOffer(
                fundedAddress, currencyOffered, 0.0, 0.0, 0 as Byte, 0.0, actionCancel)
        generateBlock()

        then:
        getTransactionMP(cancelTxid).valid

        and: "the original balance is restored"
        getbalance_MP(fundedAddress, currencyOffered).balance == balanceAtStart.balance
        getbalance_MP(fundedAddress, currencyOffered).reserved == balanceAtStart.reserved
        getbalance_MP(fundedAddress, currencyOffered) == balanceAtStart

        and: "the offer is no longer listed"
        getactivedexsells_MP().size() == offersAtStart.size()

        where:
        [startBTC, startMSC, currencyOffered, offeredMSC, desiredBTC, updatedMSC, updatedBTC] <<
                [[0.1, 1.0, MSC, 0.5, 0.5, 1.0, 2.0]]
    }

    def "An offer can be accepted with an accept transaction of type 22"() {
        given:
        def actorA = createFundedAddress(startBTC, startMSC)
        def actorB = createFundedAddress(startBTC, 0.0)

        when: "A offers MSC"
        def offerTxid = createDexSellOffer(
                actorA, currencyOffered, offeredMSC, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        and: "B accepts the offer"
        def acceptTxid = acceptDexOffer(actorB, currencyOffered, offeredMSC, actorA)
        generateBlock()

        then:
        getTransactionMP(offerTxid).valid
        getTransactionMP(acceptTxid).valid

        where:
        [startBTC, startMSC, currencyOffered, offeredMSC, desiredBTC] << [[0.1, 0.1, MSC, 0.05, 0.07]]
    }

    // TODO: actual payment (requires BTC transaction with marker)

}
