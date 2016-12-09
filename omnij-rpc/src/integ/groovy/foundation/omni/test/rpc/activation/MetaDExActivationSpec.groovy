package foundation.omni.test.rpc.activation

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import org.bitcoinj.core.Address
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Specification for the activation of the distributed token exchange.
 *
 * Once the feature with identifier 2 is active, the distributed token exchange can be used with non-test tokens.
 *
 * During the grace period, the old rules are still in place.
 *
 * Note: this test is only successful with a clean state, and requires that the feature is initially disabled!
 */
@Ignore('the tests can only be executed with a pristine state, and the MetaDEx is already activated in regtest mode')
@Stepwise
class MetaDExActivationSpec extends BaseActivationSpec {

    @Shared Integer activationBlock = 999999
    @Shared Address actorAddress
    @Shared CurrencyID mainID
    @Shared CurrencyID testID

    def beforeSpec() {
        skipIfActivated(metaDExFeatureId)
    }

    def setupSpec() {
        actorAddress = createFundedAddress(startBTC, startMSC)
        mainID = fundNewProperty(actorAddress, 10.divisible, Ecosystem.OMNI)
        testID = fundNewProperty(actorAddress, 15.divisible, Ecosystem.TOMNI)
    }

    def "Creating trades involving test ecosystem tokens before the activation is valid"() {
        setup:
        def amountForSale = 3.divisible
        def amountDesired = 5.divisible
        def balanceAtStart = omniGetBalance(actorAddress, testID)

        when:
        def tradeTxid = omniSendTrade(actorAddress, testID, amountForSale, CurrencyID.TOMNI, amountDesired)
        generate()

        then:
        omniGetTransaction(tradeTxid).valid

        and:
        omniGetTrade(tradeTxid).status == "open"
        omniGetTrade(tradeTxid).amountremaining as BigDecimal == amountForSale.numberValue()
        omniGetTrade(tradeTxid).amounttofill as BigDecimal == amountDesired.numberValue()
        omniGetTrade(tradeTxid).amountforsale as BigDecimal == amountForSale.numberValue()
        omniGetTrade(tradeTxid).amountdesired as BigDecimal == amountDesired.numberValue()
        omniGetTrade(tradeTxid).propertyidforsale == testID.getValue()
        omniGetTrade(tradeTxid).propertyiddesired == CurrencyID.TOMNI.getValue()
        omniGetTrade(tradeTxid).propertyidforsaleisdivisible
        omniGetTrade(tradeTxid).propertyiddesiredisdivisible
        omniGetTrade(tradeTxid).unitprice == "1.66666666666666666666666666666666666666666666666667"

        and:
        omniGetBalance(actorAddress, testID).balance == balanceAtStart.balance - amountForSale.numberValue()
        omniGetBalance(actorAddress, testID).reserved == balanceAtStart.reserved + amountForSale.numberValue()

        when:
        def cancelTxid = omniSendCancelAllTrades(actorAddress, Ecosystem.TOMNI)
        generate()

        then:
        omniGetTrade(cancelTxid).valid
        omniGetTrade(cancelTxid).ecosystem == "test"

        and:
        omniGetTrade(tradeTxid).status == "cancelled"
        omniGetTrade(tradeTxid).amountforsale as BigDecimal == amountForSale.numberValue()
        omniGetTrade(tradeTxid).amountdesired as BigDecimal == amountDesired.numberValue()
        omniGetTrade(tradeTxid).propertyidforsale == testID.getValue()
        omniGetTrade(tradeTxid).propertyiddesired == CurrencyID.TOMNI.getValue()

        and:
        omniGetBalance(actorAddress, testID) == balanceAtStart
    }

    def "Creating trades involving main ecosystem tokens before the activation is invalid"() {
        when:
        def txid = omniSendTrade(actorAddress, mainID, 5.divisible, CurrencyID.OMNI, 2.divisible)
        generate()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetBalance(actorAddress, mainID) == old(omniGetBalance(actorAddress, mainID))
        omniGetBalance(actorAddress, CurrencyID.OMNI) == old(omniGetBalance(actorAddress, CurrencyID.OMNI))
    }

    def "Feature identifier 2 can be used to schedule the activation of the distributed token exchange"() {
        setup:
        activationBlock = getBlockCount() + activationMinBlocks + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, metaDExFeatureId, activationBlock, minClientVersion)
        generate()

        then:
        omniGetTransaction(txid).valid

        when:
        def activations = omniGetActivations()
        def pendingActivations = activations.pendingactivations
        def completedActivations = activations.completedactivations

        then:
        pendingActivations.any { it.featureid == metaDExFeatureId }
        !completedActivations.any { it.featureid == metaDExFeatureId }

        when:
        def pendingFeature = pendingActivations.find { it.featureid == metaDExFeatureId } as Map<String, Object>

        then:
        pendingFeature.featureid == metaDExFeatureId
        pendingFeature.activationblock == activationBlock
        pendingFeature.minimumversion == minClientVersion
    }

    def "Creating trades involving main ecosystem tokens during the grace period is still invalid"() {
        when:
        def txid = omniSendTrade(actorAddress, CurrencyID.OMNI, startMSC, mainID, 25.divisible)
        generate()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetBalance(actorAddress, mainID) == old(omniGetBalance(actorAddress, mainID))
        omniGetBalance(actorAddress, CurrencyID.OMNI) == old(omniGetBalance(actorAddress, CurrencyID.OMNI))
    }

    def "After the successful activation of the feature, the feature activation completed"() {
        setup:
        while (getBlockCount() < activationBlock) {
            generate()
        }

        when:
        def activations = omniGetActivations()
        def pendingActivations = activations.pendingactivations
        def completedActivations = activations.completedactivations

        then:
        !pendingActivations.any { it.featureid == metaDExFeatureId }
        completedActivations.any { it.featureid == metaDExFeatureId }

        when:
        def activatedFeature = completedActivations.find { it.featureid == metaDExFeatureId } as Map<String, Object>

        then:
        activatedFeature.featureid == metaDExFeatureId
        activatedFeature.activationblock == activationBlock
        activatedFeature.minimumversion == minClientVersion

    }

    def "After the successful activation of the feature, it valid to trade main ecosystem tokens"() {
        setup:
        def amountForSale = startMSC
        def amountDesired = 25.divisible
        def balanceAtStart = omniGetBalance(actorAddress, CurrencyID.OMNI)

        when:
        def tradeTxid = omniSendTrade(actorAddress, CurrencyID.OMNI, amountForSale, mainID, amountDesired)
        generate()

        then:
        omniGetTransaction(tradeTxid).valid

        and:
        omniGetTrade(tradeTxid).status == "open"
        omniGetTrade(tradeTxid).amountremaining as BigDecimal == amountForSale.numberValue()
        omniGetTrade(tradeTxid).amounttofill as BigDecimal == amountDesired.numberValue()
        omniGetTrade(tradeTxid).amountforsale as BigDecimal == amountForSale.numberValue()
        omniGetTrade(tradeTxid).amountdesired as BigDecimal == amountDesired.numberValue()
        omniGetTrade(tradeTxid).propertyidforsale == CurrencyID.OMNI.getValue()
        omniGetTrade(tradeTxid).propertyiddesired == mainID.getValue()
        omniGetTrade(tradeTxid).propertyidforsaleisdivisible
        omniGetTrade(tradeTxid).propertyiddesiredisdivisible
        omniGetTrade(tradeTxid).unitprice == "0.00400000000000000000000000000000000000000000000000"

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == balanceAtStart.balance - amountForSale
        omniGetBalance(actorAddress, CurrencyID.OMNI).reserved == balanceAtStart.reserved + amountForSale

        when:
        def cancelTxid = omniSendCancelTradesByPrice(actorAddress, CurrencyID.OMNI, amountForSale, mainID, amountDesired)
        generate()

        then:
        omniGetTransaction(cancelTxid).valid

        and:
        omniGetTrade(tradeTxid).status == "cancelled"

        and:
        omniGetTrade(cancelTxid).propertyidforsale == CurrencyID.OMNI.getValue()
        omniGetTrade(cancelTxid).propertyidforsaleisdivisible
        omniGetTrade(cancelTxid).amountforsale as BigDecimal == amountForSale.numberValue()
        omniGetTrade(cancelTxid).propertyiddesired == mainID.getValue()
        omniGetTrade(cancelTxid).propertyiddesiredisdivisible
        omniGetTrade(cancelTxid).amountdesired as BigDecimal == amountDesired.numberValue()
        omniGetTrade(cancelTxid).unitprice == "0.00400000000000000000000000000000000000000000000000"

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI) == balanceAtStart
    }

}
