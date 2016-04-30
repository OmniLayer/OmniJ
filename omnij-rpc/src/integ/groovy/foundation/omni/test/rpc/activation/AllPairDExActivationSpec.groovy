package foundation.omni.test.rpc.activation

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import org.bitcoinj.core.Address
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Specification for the activation of the all pair trading of the distributed token exchange.
 *
 * Once the feature with identifier 8 is active, the distributed token exchange can be used with non-Omni tokens.
 *
 * During the grace period, the old rules are still in place.
 *
 * Note: this test is only successful with a clean state, and requires that the feature is initially disabled!
 */
@Stepwise
class AllPairDExActivationSpec extends BaseActivationSpec {

    @Shared Integer activationBlock = 999999
    @Shared Address actorAddress
    @Shared CurrencyID tokenA
    @Shared CurrencyID tokenB

    def beforeSpec() {
        skipIfActivated(allPairDExFeatureId)
    }

    def setupSpec() {
        actorAddress = createFundedAddress(startBTC, startMSC)
        tokenA = fundNewProperty(actorAddress, 445.indivisible, Ecosystem.MSC)
        tokenB = fundNewProperty(actorAddress, 20.indivisible, Ecosystem.MSC)
    }

    def "Creating trades involving two non-Omni pairs before the activation is invalid"() {
        when:
        def txid = omniSendTrade(actorAddress, tokenA, 445.indivisible, tokenB, 20.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetBalance(actorAddress, tokenA) == old(omniGetBalance(actorAddress, tokenA))
        omniGetBalance(actorAddress, tokenB) == old(omniGetBalance(actorAddress, tokenB))
    }

    def "Feature identifier 8 can be used to schedule the activation of non-Omni pair trading"() {
        setup:
        activationBlock = getBlockCount() + activationMinBlocks + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, allPairDExFeatureId, activationBlock, minClientVersion)
        generateBlock()

        then:
        omniGetTransaction(txid).valid

        when:
        def activations = omniGetActivations()
        def pendingActivations = activations.pendingactivations
        def completedActivations = activations.completedactivations

        then:
        pendingActivations.any { it.featureid == allPairDExFeatureId }
        !completedActivations.any { it.featureid == allPairDExFeatureId }

        when:
        def pendingFeature = pendingActivations.find { it.featureid == allPairDExFeatureId } as Map<String, Object>

        then:
        pendingFeature.featureid == allPairDExFeatureId
        pendingFeature.activationblock == activationBlock
        pendingFeature.minimumversion == minClientVersion
    }

    def "Creating trades involving non-Omni tokens during the grace period is still invalid"() {
        when:
        def txid = omniSendTrade(actorAddress, tokenA, 445.indivisible, tokenB, 20.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetBalance(actorAddress, tokenA) == old(omniGetBalance(actorAddress, tokenA))
        omniGetBalance(actorAddress, tokenB) == old(omniGetBalance(actorAddress, tokenB))
    }

    def "After the successful activation of the feature, the feature activation completed"() {
        setup:
        while (getBlockCount() < activationBlock) {
            generateBlock()
        }

        when:
        def activations = omniGetActivations()
        def pendingActivations = activations.pendingactivations
        def completedActivations = activations.completedactivations

        then:
        !pendingActivations.any { it.featureid == allPairDExFeatureId }
        completedActivations.any { it.featureid == allPairDExFeatureId }

        when:
        def activatedFeature = completedActivations.find { it.featureid == allPairDExFeatureId } as Map<String, Object>

        then:
        activatedFeature.featureid == allPairDExFeatureId
        activatedFeature.activationblock == activationBlock
        activatedFeature.minimumversion == minClientVersion

    }

    def "After the successful activation of the feature, it valid to trade non-Omni tokens"() {
        setup:
        def amountForSale = 2.indivisible
        def amountDesired = 4.indivisible
        def balanceAtStart = omniGetBalance(actorAddress, tokenA)

        when:
        def tradeTxid = omniSendTrade(actorAddress, tokenA, amountForSale, tokenB, amountDesired)
        generateBlock()

        then:
        omniGetTransaction(tradeTxid).valid

        and:
        omniGetTrade(tradeTxid).status == "open"
        omniGetTrade(tradeTxid).amountremaining as Long == amountForSale.numberValue()
        omniGetTrade(tradeTxid).amounttofill as Long == amountDesired.numberValue()
        omniGetTrade(tradeTxid).amountforsale as Long == amountForSale.numberValue()
        omniGetTrade(tradeTxid).amountdesired as Long == amountDesired.numberValue()
        omniGetTrade(tradeTxid).propertyidforsale == tokenA.getValue()
        omniGetTrade(tradeTxid).propertyiddesired == tokenB.getValue()
        !omniGetTrade(tradeTxid).propertyidforsaleisdivisible
        !omniGetTrade(tradeTxid).propertyiddesiredisdivisible
        omniGetTrade(tradeTxid).unitprice == "2.00000000000000000000000000000000000000000000000000"

        and:
        omniGetBalance(actorAddress, tokenA).balance == balanceAtStart.balance - amountForSale
        omniGetBalance(actorAddress, tokenA).reserved == balanceAtStart.reserved + amountForSale

        when:
        def cancelTxid = omniSendCancelTradesByPrice(actorAddress, tokenA, amountForSale, tokenB, amountDesired)
        generateBlock()

        then:
        omniGetTransaction(cancelTxid).valid

        and:
        omniGetTrade(tradeTxid).status == "cancelled"

        and:
        omniGetTrade(cancelTxid).propertyidforsale == tokenA.getValue()
        !omniGetTrade(cancelTxid).propertyidforsaleisdivisible
        omniGetTrade(cancelTxid).amountforsale as Long == amountForSale.numberValue()
        omniGetTrade(cancelTxid).propertyiddesired == tokenB.getValue()
        !omniGetTrade(cancelTxid).propertyiddesiredisdivisible
        omniGetTrade(cancelTxid).amountdesired as Long == amountDesired.numberValue()
        omniGetTrade(cancelTxid).unitprice == "2.00000000000000000000000000000000000000000000000000"

        and:
        omniGetBalance(actorAddress, tokenA) == balanceAtStart
    }

}
