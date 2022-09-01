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
        generateBlocks(1)

        then:
        omniGetTransaction(tradeTxid).valid

        and:
        omniGetTrade(tradeTxid).status == "open"
        omniGetTrade(tradeTxid).amountRemaining == amountForSale
        omniGetTrade(tradeTxid).amountToFill == amountDesired
        omniGetTrade(tradeTxid).amountForSale == amountForSale
        omniGetTrade(tradeTxid).amountDesired == amountDesired
        omniGetTrade(tradeTxid).propertyIdForSale == testID
        omniGetTrade(tradeTxid).propertyIdDesired == CurrencyID.TOMNI
        omniGetTrade(tradeTxid).propertyIdForSaleIsDivisible
        omniGetTrade(tradeTxid).propertyIdDesiredIsDivisble
        omniGetTrade(tradeTxid).unitPrice == "1.66666666666666666666666666666666666666666666666667"

        and:
        omniGetBalance(actorAddress, testID).balance == balanceAtStart.balance - amountForSale.numberValue()
        omniGetBalance(actorAddress, testID).reserved == balanceAtStart.reserved + amountForSale.numberValue()

        when:
        def cancelTxid = omniSendCancelAllTrades(actorAddress, Ecosystem.TOMNI)
        generateBlocks(1)

        then:
        omniGetTrade(cancelTxid).valid
        //omniGetTrade(cancelTxid).ecosystem == "test"

        and:
        omniGetTrade(tradeTxid).status == "cancelled"
        omniGetTrade(tradeTxid).amountForSale == amountForSale
        omniGetTrade(tradeTxid).amountDesired == amountDesired
        omniGetTrade(tradeTxid).propertyIdForSale == testID
        omniGetTrade(tradeTxid).propertyIdDesired == CurrencyID.TOMNI

        and:
        omniGetBalance(actorAddress, testID) == balanceAtStart
    }

    def "Creating trades involving main ecosystem tokens before the activation is invalid"() {
        when:
        def txid = omniSendTrade(actorAddress, mainID, 5.divisible, CurrencyID.OMNI, 2.divisible)
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid

        and:
        omniGetBalance(actorAddress, mainID) == old(omniGetBalance(actorAddress, mainID))
        omniGetBalance(actorAddress, CurrencyID.OMNI) == old(omniGetBalance(actorAddress, CurrencyID.OMNI))
    }

    def "Feature identifier 2 can be used to schedule the activation of the distributed token exchange"() {
        setup:
        activationBlock = getBlockCount() + activationMinBlocks + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, metaDExFeatureId, activationBlock, minClientVersion)
        generateBlocks(1)

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
        generateBlocks(1)

        then:
        !omniGetTransaction(txid).valid

        and:
        omniGetBalance(actorAddress, mainID) == old(omniGetBalance(actorAddress, mainID))
        omniGetBalance(actorAddress, CurrencyID.OMNI) == old(omniGetBalance(actorAddress, CurrencyID.OMNI))
    }

    def "After the successful activation of the feature, the feature activation completed"() {
        setup:
        while (getBlockCount() < activationBlock) {
            generateBlocks(1)
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
        generateBlocks(1)

        then:
        omniGetTransaction(tradeTxid).valid

        and:
        omniGetTrade(tradeTxid).status == "open"
        omniGetTrade(tradeTxid).amountRemaining == amountForSale
        omniGetTrade(tradeTxid).amountToFill == amountDesired
        omniGetTrade(tradeTxid).amountForSale == amountForSale
        omniGetTrade(tradeTxid).amountDesired == amountDesired
        omniGetTrade(tradeTxid).propertyIdForSale == CurrencyID.OMNI
        omniGetTrade(tradeTxid).propertyIdDesired == mainID
        omniGetTrade(tradeTxid).propertyIdForSaleIsDivisible
        omniGetTrade(tradeTxid).propertyIdDesiredIsDivisble
        omniGetTrade(tradeTxid).unitPrice == "0.00400000000000000000000000000000000000000000000000"

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == balanceAtStart.balance - amountForSale
        omniGetBalance(actorAddress, CurrencyID.OMNI).reserved == balanceAtStart.reserved + amountForSale

        when:
        def cancelTxid = omniSendCancelTradesByPrice(actorAddress, CurrencyID.OMNI, amountForSale, mainID, amountDesired)
        generateBlocks(1)

        then:
        omniGetTransaction(cancelTxid).valid

        and:
        omniGetTrade(tradeTxid).status == "cancelled"

        and:
        omniGetTrade(cancelTxid).propertyIdForSale == CurrencyID.OMNI
        omniGetTrade(cancelTxid).propertyIdForSaleIsDivisible
        omniGetTrade(cancelTxid).amountForSale == amountForSale
        omniGetTrade(cancelTxid).propertyIdDesired == mainID
        omniGetTrade(cancelTxid).propertyIdDesiredIsDivisble
        omniGetTrade(cancelTxid).amountDesired == amountDesired
        omniGetTrade(cancelTxid).unitPrice == "0.00400000000000000000000000000000000000000000000000"

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI) == balanceAtStart
    }

}
