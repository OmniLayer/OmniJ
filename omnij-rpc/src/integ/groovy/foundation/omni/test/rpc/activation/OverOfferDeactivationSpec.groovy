package foundation.omni.test.rpc.activation

import foundation.omni.CurrencyID
import foundation.omni.OmniDivisibleValue
import org.bitcoinj.core.Coin
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Specification for the deactivation of "over-offers" on the traditional distributed exchange.
 *
 * Once the feature with identifier 5 is active, it is no longer allowed to create offers on the traditional
 * distributed exchange for an amount that is more than the available balance.
 *
 * During the grace period, the old rules are still in place.
 */
@Stepwise
class OverOfferDeactivationSpec extends BaseActivationSpec {

    static final Coin stdCommitFee = 0.btc
    static final Byte stdBlockSpan = 10
    static final Byte actionNew = 1

    @Shared Integer activationBlock = 999999

    def beforeSpec() {
        skipIfActivated(overOffersFeatureId)
    }

    def "Offering more than available on the distributed exchange is valid before the deactivation"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(actorAddress, CurrencyID.OMNI).balance
        def orderAmountMSC = (OmniDivisibleValue) balanceAtStart * 5 // more than available!
        def orderAmountBTC = Coin.valueOf(orderAmountMSC.willetts)
        when:
        def txid = createDexSellOffer(
                actorAddress, CurrencyID.OMNI, orderAmountMSC, orderAmountBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).amount != orderAmountMSC
        omniGetTransaction(txid).amount == balanceAtStart // less than offered!

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance.numberValue() == 0.0
        omniGetBalance(actorAddress, CurrencyID.OMNI).reserved.numberValue() == balanceAtStart.numberValue()
    }

    def "Feature identifier 5 can be used to schedule the deactivation of \"over-offers\""() {
        setup:
        def actorAddress = createFundedAddress(startBTC, zeroAmount)
        activationBlock = getBlockCount() + activationMinBlocks + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, overOffersFeatureId, activationBlock, minClientVersion)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).valid
    }

    def "Offering more than available on the distributed exchange is still valid until the feature activation"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(actorAddress, CurrencyID.OMNI).balance
        def orderAmountMSC = (OmniDivisibleValue) balanceAtStart * 5 // more than available!
        def orderAmountBTC = Coin.valueOf(orderAmountMSC.willetts)
        def blockBeforeActivation = activationBlock - 1

        while (getBlockCount() < blockBeforeActivation - 1) { // two extra, for transaction confirmation
            generateBlocks(1)
        }

        when:
        def txid = createDexSellOffer(
                actorAddress, CurrencyID.OMNI, orderAmountMSC, orderAmountBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).amount != orderAmountMSC
        omniGetTransaction(txid).amount == balanceAtStart // less than offered!

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance.numberValue() == 0.0
        omniGetBalance(actorAddress, CurrencyID.OMNI).reserved.numberValue() == balanceAtStart.numberValue()
    }

    def "After the successful activation of the feature, it is no longer valid to offer more than available"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(actorAddress, CurrencyID.OMNI).balance
        def orderAmountMSC = (OmniDivisibleValue) balanceAtStart * 5 // more than available!
        def orderAmountBTC = Coin.valueOf(orderAmountMSC.willetts)

        when:
        def txid = createDexSellOffer(
                actorAddress, CurrencyID.OMNI, orderAmountMSC, orderAmountBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance.numberValue() == balanceAtStart.numberValue()
        omniGetBalance(actorAddress, CurrencyID.OMNI).reserved.numberValue() == 0.0
    }

    def "The activation has no effect on orders, which offer exactly the balance that is available"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(actorAddress, CurrencyID.OMNI).balance
        def orderAmountMSC = (OmniDivisibleValue) balanceAtStart
        def orderAmountBTC = Coin.valueOf(orderAmountMSC.willetts)

        when:
        def txid = createDexSellOffer(
                actorAddress, CurrencyID.OMNI, orderAmountMSC, orderAmountBTC, stdBlockSpan, stdCommitFee, actionNew)
        generateBlocks(1)

        then:
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).amount == orderAmountMSC

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance.numberValue() == 0.0
        omniGetBalance(actorAddress, CurrencyID.OMNI).reserved.numberValue() == orderAmountMSC.numberValue()
    }
}
