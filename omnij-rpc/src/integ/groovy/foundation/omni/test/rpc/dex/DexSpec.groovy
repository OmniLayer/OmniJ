package foundation.omni.test.rpc.dex

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import org.bitcoinj.core.Coin
import spock.lang.Unroll

import static CurrencyID.MSC
import static CurrencyID.TMSC

/**
 * Specification for the traditional distributed exchange
 */
class DexSpec extends BaseRegTestSpec {

    final static Coin stdCommitFee = 0.0001.btc
    final static Byte stdBlockSpan = 10
    final static Byte actionNew = 1
    final static Byte actionUpdate = 2
    final static Byte actionCancel = 3

    def "A new sell offer can be created with action = 1 (new)"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)
        def activeOffersAtTheStart = omniGetActiveDExSells()

        when: "creating an offer with action = 1"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        and: "retrieving information about the offer"
        def offerTx = omniGetTransaction(offerTxid)

        then: "it is a valid transaction"
        offerTx.txid == offerTxid.toString()
        offerTx.sendingaddress == fundedAddress.toString()
        offerTx.version == 1
        offerTx.type_int == 20
        offerTx.type == "DEx Sell Offer"
        offerTx.propertyid == currencyOffered.getValue()
        offerTx.divisible
        offerTx.amount as BigDecimal == amountOffered
        offerTx.bitcoindesired as BigDecimal == desiredBTC
        offerTx.timelimit == stdBlockSpan
        offerTx.feerequired as BigDecimal == stdCommitFee.decimalBtc
        offerTx.action == "new"
        offerTx.valid
        offerTx.confirmations == 1

        and: "a new offer is created on the distributed exchange"
        def activeOffersNow = omniGetActiveDExSells()
        activeOffersNow.size() == activeOffersAtTheStart.size() + 1

        where:
        [startBTC, startMSC, currencyOffered, amountOffered, desiredBTC] << [[0.1, 2.5, MSC, 1.0, 0.2]]
    }

    @Unroll
    def "An accepted currency identifier for sell offers is #currencyOffered"(CurrencyID currencyOffered) {
        given:
        def startBTC = 0.1
        def startMSC = 2.5
        def amountOffered = 1.0
        def desiredBTC = 0.2
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)

        when: "an offer of #currencyId is created"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        then: "the transaction should be a valid offering of #currencyOffered"
        def offerTx = omniGetTransaction(offerTxid)
        offerTx.valid == true
        offerTx.propertyid == currencyOffered.getValue()

        where: "the currency identifier is either MSC or TMSC"
        currencyOffered << [MSC, TMSC]
    }

    def "Offering more tokens than available puts up an offer with the available amount"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        BigDecimal amountAvailableAtStart = omniGetBalance(fundedAddress, currencyOffered).balance
        BigDecimal amountOffered = amountAvailableAtStart + 100.0

        when: "the amount offered for sale exceeds the sending address's available balance"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        then: "this indicates to sell all tokens that are available"
        def offerTx = omniGetTransaction(offerTxid)
        def offerAmount = new BigDecimal(offerTx.amount)
        def amountAvailableNow = omniGetBalance(fundedAddress, currencyOffered).balance
        offerTx.valid == true
        offerAmount == amountAvailableAtStart
        offerAmount == amountOffered.min(amountAvailableAtStart)
        amountAvailableNow == 0.0

        where:
        [startBTC, startMSC, currencyOffered, desiredBTC] << [[0.1, 2.5, MSC, 50.0]]
    }

    def "The amount offered for sale is reserved from the available balance"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(fundedAddress, currencyOffered)

        when: "an amount is offered for sale"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        then: "the offered amount is reserved and subtracted from the available balance"
        def offerTx = omniGetTransaction(offerTxid)
        def offerAmount = new BigDecimal(offerTx.amount)
        def balanceNow = omniGetBalance(fundedAddress, currencyOffered)
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
                fundedAddress, currencyOffered, offerMSC, desiredBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        and: "additional tokens are received"
        def offerBeforeReceivingMore = omniGetTransaction(offerTxid)
        def balanceBeforeReceivingMore = omniGetBalance(fundedAddress, currencyOffered)
        def otherAddress = createFundedAddress(startBTC, startOtherMSC)
        def sendTxid = omniSend(otherAddress, fundedAddress, currencyOffered, additionalMSC)
        generateBlock()

        then: "any tokens received are added to the available balance"
        def balanceNow = omniGetBalance(fundedAddress, currencyOffered)
        def sendTx = omniGetTransaction(sendTxid)
        def sendAmount = new BigDecimal(sendTx.amount)
        sendTx.valid == true
        balanceNow.balance == balanceBeforeReceivingMore.balance + sendAmount

        and: "are not included in the amount for sale by this sell offer"
        def offerNow = omniGetTransaction(offerTxid)
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
                fundedAddress, currencyOffered, firstOfferMSC, firstOfferBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        and: "and another offer accepting BTC is made"
        def secondOfferTxid = createDexSellOffer(
                fundedAddress, currencyOffered, secondOfferMSC, secondOfferBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        then: "the other offer is rejected"
        omniGetTransaction(firstOfferTxid).valid == true
        omniGetTransaction(secondOfferTxid).valid == false

        where:
        [startBTC, startMSC, currencyOffered, firstOfferMSC, firstOfferBTC,
         secondOfferMSC, secondOfferBTC] << [[0.1, 2.5, MSC, 1.0, 0.2, 1.5, 0.3]]
    }

    def "An offer can be updated with action = 2 (update), and cancelled with action = 3 (cancel)"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(fundedAddress, currencyOffered)
        def offersAtStart = omniGetActiveDExSells()

        when: "creating an offer with action 1"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, offeredMSC, desiredBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        then:
        omniGetTransaction(offerTxid).valid
        omniGetTransaction(offerTxid).amount as BigDecimal == offeredMSC

        and:
        omniGetBalance(fundedAddress, currencyOffered).balance == balanceAtStart.balance - offeredMSC
        omniGetBalance(fundedAddress, currencyOffered).reserved == balanceAtStart.reserved + offeredMSC

        and: "a new offer is listed"
        omniGetActiveDExSells().size() == offersAtStart.size() + 1

        when: "updating an offer with action = 2"
        def updateTxid = createDexSellOffer(
                fundedAddress, currencyOffered, updatedMSC, updatedBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionUpdate)
        generateBlock()

        and: "retrieving information about the update"
        def updateTx = omniGetTransaction(updateTxid)

        then: "the offered amount is updated"
        updateTx.amount as BigDecimal == updatedMSC
        updateTx.action == "update"
        updateTx.valid

        and: "the total amount offered is reserved"
        omniGetBalance(fundedAddress, currencyOffered).balance == balanceAtStart.balance - updatedMSC
        omniGetBalance(fundedAddress, currencyOffered).reserved == balanceAtStart.reserved + updatedMSC

        when: "cancelling an offer with action = 3"
        def cancelTxid = createDexSellOffer(
                fundedAddress, currencyOffered, 0.0, 0.0, 0 as Byte, 0.0, actionCancel)
        generateBlock()

        and: "retrieving information about the cancel"
        def cancelTx = omniGetTransaction(cancelTxid)

        then: "the transaction is valid"
        cancelTx.valid
        cancelTx.action == "cancel"

        and: "the original balance is restored"
        omniGetBalance(fundedAddress, currencyOffered).balance == balanceAtStart.balance
        omniGetBalance(fundedAddress, currencyOffered).reserved == balanceAtStart.reserved
        omniGetBalance(fundedAddress, currencyOffered) == balanceAtStart

        and: "the offer is no longer listed"
        omniGetActiveDExSells().size() == offersAtStart.size()

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
                actorA, currencyOffered, offeredMSC, desiredBTC, stdBlockSpan, stdCommitFee.decimalBtc, actionNew)
        generateBlock()

        and: "B accepts the offer"
        def acceptTxid = acceptDexOffer(actorB, currencyOffered, offeredMSC, actorA)
        generateBlock()

        then: "both transactions are valid"
        omniGetTransaction(offerTxid).valid
        omniGetTransaction(acceptTxid).valid

        when: "retrieving information about the accept order"
        def acceptTx = omniGetTransaction(acceptTxid)

        then: "the information matches the specified data"
        acceptTx.txid == acceptTxid.toString()
        acceptTx.sendingaddress == actorB.toString()
        acceptTx.referenceaddress == actorA.toString()
        acceptTx.version == 0
        acceptTx.type_int == 22
        acceptTx.type == "DEx Accept Offer"
        acceptTx.propertyid == currencyOffered.getValue()
        acceptTx.divisible
        acceptTx.amount as BigDecimal == offeredMSC
        acceptTx.confirmations == 1

        where:
        [startBTC, startMSC, currencyOffered, offeredMSC, desiredBTC] << [[0.1, 0.1, MSC, 0.05, 0.07]]
    }

    // TODO: actual payment (requires BTC transaction with marker)

}
