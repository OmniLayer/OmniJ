package foundation.omni.test.rpc.dex

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.OmniDivisibleValue
import org.bitcoinj.base.Coin
import spock.lang.Unroll

import static foundation.omni.CurrencyID.OMNI
import static foundation.omni.CurrencyID.TOMNI

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
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        and: "retrieving information about the offer"
        def offerTx = omniGetTransaction(offerTxid)

        then: "it is a valid transaction"
        offerTx.txId == offerTxid
        offerTx.sendingAddress == fundedAddress
        offerTx.version == 1
        offerTx.typeInt == 20
        offerTx.type == "DEx Sell Offer"
        offerTx.propertyId == currencyOffered
        offerTx.divisible
        offerTx.amount == amountOffered
        offerTx.otherInfo.bitcoindesired as BigDecimal == desiredBTC.toBtc()
        offerTx.otherInfo.timelimit == stdBlockSpan
        offerTx.otherInfo.feerequired as BigDecimal == stdCommitFee.toBtc()
        offerTx.otherInfo.action == "new"
        offerTx.valid
        offerTx.confirmations == 1

        and: "a new offer is created on the distributed exchange"
        def activeOffersNow = omniGetActiveDExSells()
        activeOffersNow.size() == activeOffersAtTheStart.size() + 1

        where:
        [startBTC, startMSC, currencyOffered, amountOffered, desiredBTC] << [[0.1.btc, 2.5.divisible, OMNI, 1.0.divisible, 0.2.btc]]
    }

    @Unroll
    def "An accepted currency identifier for sell offers is #currencyOffered"(CurrencyID currencyOffered) {
        given:
        def startBTC = 0.1.btc
        def startMSC = 2.5.divisible
        def amountOffered = 1.0.divisible
        def desiredBTC = 0.2.btc
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)

        when: "an offer of #currencyId is created"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then: "the transaction should be a valid offering of #currencyOffered"
        def offerTx = omniGetTransaction(offerTxid)
        offerTx.valid == true
        offerTx.propertyId == currencyOffered

        where: "the currency identifier is either OMNI or TOMNI"
        currencyOffered << [OMNI, TOMNI]
    }

    def "Offering more tokens than available puts up an offer with the available amount"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        OmniDivisibleValue amountAvailableAtStart = (OmniDivisibleValue) omniGetBalance(fundedAddress, currencyOffered).balance
        OmniDivisibleValue amountOffered = amountAvailableAtStart + 100.divisible

        when: "the amount offered for sale exceeds the sending address's available balance"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then: "this indicates to sell all tokens that are available"
        def offerTx = omniGetTransaction(offerTxid)
        def offerAmount = offerTx.amount
        def amountAvailableNow = omniGetBalance(fundedAddress, currencyOffered).balance
        offerTx.valid == true
        offerAmount.equals(amountAvailableAtStart)
        offerAmount.equals(min(amountOffered, amountAvailableAtStart))
        amountAvailableNow.equals(0.divisible)

        where:
        [startBTC, startMSC, currencyOffered, desiredBTC] << [[0.1.btc, 2.5.divisible, OMNI, 50.btc]]
    }

    def "The amount offered for sale is reserved from the available balance"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(fundedAddress, currencyOffered)

        when: "an amount is offered for sale"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, amountOffered, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then: "the offered amount is reserved and subtracted from the available balance"
        def offerTx = omniGetTransaction(offerTxid)
        def offerAmount = offerTx.amount
        def balanceNow = omniGetBalance(fundedAddress, currencyOffered)
        offerTx.valid == true
        balanceNow.balance.equals(balanceAtStart.balance - offerAmount)
        balanceNow.reserved.equals(balanceAtStart.reserved + offerAmount)

        where:
        [startBTC, startMSC, currencyOffered, amountOffered, desiredBTC] << [[0.1.btc, 100.divisible, OMNI, 90.divisible, 45.btc]]
    }

    def "Receiving tokens doesn't increase the offered amount of a published offer"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)

        when: "the sell offer is published"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, offerMSC, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        and: "additional tokens are received"
        def offerBeforeReceivingMore = omniGetTransaction(offerTxid)
        def balanceBeforeReceivingMore = omniGetBalance(fundedAddress, currencyOffered)
        def otherAddress = createFundedAddress(startBTC, startOtherMSC)
        def sendTxid = omniSend(otherAddress, fundedAddress, currencyOffered, additionalMSC)
        generateBlocks(1)

        then: "any tokens received are added to the available balance"
        def balanceNow = omniGetBalance(fundedAddress, currencyOffered)
        def sendTx = omniGetTransaction(sendTxid)
        def sendAmount = sendTx.amount
        sendTx.valid == true
        balanceNow.balance.equals(balanceBeforeReceivingMore.balance + sendAmount)

        and: "are not included in the amount for sale by this sell offer"
        def offerNow = omniGetTransaction(offerTxid)
        offerNow.valid == true
        offerNow.amount == offerBeforeReceivingMore.amount
        balanceNow.reserved.equals(balanceBeforeReceivingMore.reserved)

        where:
        [startBTC, startMSC, currencyOffered, offerMSC, desiredBTC,
         startOtherMSC, additionalMSC] << [[0.1.btc, 2.5.divisible, OMNI, 90.divisible, 45.btc, 10.divisible, 10.divisible]]
    }

    def "There can be only one active offer that accepts BTC"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC, false)

        when: "there is already an active offer accepting BTC"
        def firstOfferTxid = createDexSellOffer(
                fundedAddress, currencyOffered, firstOfferMSC, firstOfferBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        and: "and another offer accepting BTC is made"
        def secondOfferTxid = createDexSellOffer(
                fundedAddress, currencyOffered, secondOfferMSC, secondOfferBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then: "the other offer is rejected"
        omniGetTransaction(firstOfferTxid).valid == true
        omniGetTransaction(secondOfferTxid).valid == false

        where:
        [startBTC, startMSC, currencyOffered, firstOfferMSC, firstOfferBTC,
         secondOfferMSC, secondOfferBTC] << [[0.1.btc, 2.5.divisible, OMNI, 1.divisible, 0.2.btc, 1.5.divisible, 0.3.btc]]
    }

    def "An offer can be updated with action = 2 (update), and cancelled with action = 3 (cancel)"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(fundedAddress, currencyOffered)
        def offersAtStart = omniGetActiveDExSells()

        when: "creating an offer with action 1"
        def offerTxid = createDexSellOffer(
                fundedAddress, currencyOffered, offeredMSC, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then:
        omniGetTransaction(offerTxid).valid
        omniGetTransaction(offerTxid).amount == offeredMSC

        and:
        omniGetBalance(fundedAddress, currencyOffered).balance.equals(balanceAtStart.balance - offeredMSC)
        omniGetBalance(fundedAddress, currencyOffered).reserved.equals(balanceAtStart.reserved + offeredMSC)

        and: "a new offer is listed"
        omniGetActiveDExSells().size() == offersAtStart.size() + 1

        when: "updating an offer with action = 2"
        def updateTxid = createDexSellOffer(
                fundedAddress, currencyOffered, updatedMSC, updatedBTC, stdBlockSpan, stdCommitFee, actionUpdate)
        generateBlocks(1)

        and: "retrieving information about the update"
        def updateTx = omniGetTransaction(updateTxid)

        then: "the offered amount is updated"
        updateTx.amount == updatedMSC
        updateTx.otherInfo.action == "update"
        updateTx.valid

        and: "the total amount offered is reserved"
        omniGetBalance(fundedAddress, currencyOffered).balance.equals(balanceAtStart.balance - updatedMSC)
        omniGetBalance(fundedAddress, currencyOffered).reserved.equals(balanceAtStart.reserved + updatedMSC)

        when: "cancelling an offer with action = 3"
        def cancelTxid = createDexSellOffer(
                fundedAddress, currencyOffered, 0.divisible, 0.btc, 0 as Byte, 0.btc, actionCancel)
        generateBlocks(1)

        and: "retrieving information about the cancel"
        def cancelTx = omniGetTransaction(cancelTxid)

        then: "the transaction is valid"
        cancelTx.valid
        cancelTx.otherInfo.action == "cancel"

        and: "the original balance is restored"
        omniGetBalance(fundedAddress, currencyOffered).balance .equals(balanceAtStart.balance)
        omniGetBalance(fundedAddress, currencyOffered).reserved.equals(balanceAtStart.reserved)
        omniGetBalance(fundedAddress, currencyOffered).equals(balanceAtStart)

        and: "the offer is no longer listed"
        omniGetActiveDExSells().size() == offersAtStart.size()

        where:
        [startBTC, startMSC, currencyOffered, offeredMSC, desiredBTC, updatedMSC, updatedBTC] <<
                [[0.1.btc, 1.0.divisible, OMNI, 0.5.divisible, 0.5.btc, 1.0.divisible, 2.0.btc]]
    }

    def "An offer can be accepted with an accept transaction of type 22"() {
        given:
        def actorA = createFundedAddress(startBTC, startMSC)
        def actorB = createFundedAddress(startBTC, 0.divisible)

        when: "A offers OMNI"
        def offerTxid = createDexSellOffer(
                actorA, currencyOffered, offeredMSC, desiredBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        and: "B accepts the offer"
        def acceptTxid = omniSendDExAccept(actorB, actorA, currencyOffered, offeredMSC, false)
        generateBlocks(1)

        then: "both transactions are valid"
        omniGetTransaction(offerTxid).valid
        omniGetTransaction(acceptTxid).valid

        when: "retrieving information about the accept order"
        def acceptTx = omniGetTransaction(acceptTxid)

        then: "the information matches the specified data"
        acceptTx.txId == acceptTxid
        acceptTx.sendingAddress == actorB
        acceptTx.referenceAddress == actorA
        acceptTx.version == 0
        acceptTx.typeInt == 22
        acceptTx.type == "DEx Accept Offer"
        acceptTx.propertyId == currencyOffered
        acceptTx.divisible
        acceptTx.amount == offeredMSC
        acceptTx.confirmations == 1

        where:
        [startBTC, startMSC, currencyOffered, offeredMSC, desiredBTC] << [[0.1.btc, 0.1.divisible, OMNI, 0.05.divisible, 0.07.btc]]
    }

    // TODO: actual payment (requires BTC transaction with marker)

    // Should we add this directly to Omni*Value classes?
    private static OmniDivisibleValue min(OmniDivisibleValue a, OmniDivisibleValue b)  {
        return (a.compareTo(b) <= 0 ? a : b);
    }

}
