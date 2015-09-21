package foundation.omni.test.rpc.activation

import foundation.omni.CurrencyID
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

    static final BigDecimal stdCommitFee = 0.0
    static final Byte stdBlockSpan = 10
    static final Byte actionNew = 1

    @Shared Integer activationBlock = 999999

    def beforeSpec() {
        skipIfActivated(overOffersFeatureId)
    }

    def "Offering more than available on the distributed exchange is valid before the deactivation"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(actorAddress, CurrencyID.MSC).balance
        def orderAmount = balanceAtStart * 5 // more than available!

        when:
        def txid = createDexSellOffer(
                actorAddress, CurrencyID.MSC, orderAmount, orderAmount, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        then:
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).amount as BigDecimal != orderAmount
        omniGetTransaction(txid).amount as BigDecimal == balanceAtStart // less than offered!

        and:
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == zeroAmount
        omniGetBalance(actorAddress, CurrencyID.MSC).reserved == balanceAtStart
    }

    def "Feature identifier 5 can be used to schedule the deactivation of \"over-offers\""() {
        setup:
        def actorAddress = createFundedAddress(startBTC, zeroAmount)
        activationBlock = getBlockCount() + activationMinBlocks + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, overOffersFeatureId, activationBlock, minClientVersion)
        generateBlock()

        then:
        omniGetTransaction(txid).valid
    }

    def "Offering more than available on the distributed exchange is still valid until the feature activation"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(actorAddress, CurrencyID.MSC).balance
        def orderAmount = balanceAtStart * 5 // more than available!
        def blockBeforeActivation = activationBlock - 1

        while (getBlockCount() < blockBeforeActivation - 1) { // two extra, for transaction confirmation
            generateBlock()
        }

        when:
        def txid = createDexSellOffer(
                actorAddress, CurrencyID.MSC, orderAmount, orderAmount, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        then:
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).amount as BigDecimal != orderAmount
        omniGetTransaction(txid).amount as BigDecimal == balanceAtStart // less than offered!

        and:
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == zeroAmount
        omniGetBalance(actorAddress, CurrencyID.MSC).reserved == balanceAtStart
    }

    def "After the successful activation of the feature, it is no longer valid to offer more than available"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def balanceAtStart = omniGetBalance(actorAddress, CurrencyID.MSC).balance
        def orderAmount = balanceAtStart * 5 // more than available!

        when:
        def txid = createDexSellOffer(
                actorAddress, CurrencyID.MSC, orderAmount, orderAmount, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == balanceAtStart
        omniGetBalance(actorAddress, CurrencyID.MSC).reserved == zeroAmount
    }

    def "The activation has no effect on orders, which offer exactly the balance that is available"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def orderAmount = omniGetBalance(actorAddress, CurrencyID.MSC).balance

        when:
        def txid = createDexSellOffer(
                actorAddress, CurrencyID.MSC, orderAmount, orderAmount, stdBlockSpan, stdCommitFee, actionNew)
        generateBlock()

        then:
        omniGetTransaction(txid).valid
        omniGetTransaction(txid).amount as BigDecimal == orderAmount

        and:
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == zeroAmount
        omniGetBalance(actorAddress, CurrencyID.MSC).reserved == orderAmount
    }
}
