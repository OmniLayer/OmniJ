package foundation.omni.test.rpc.activation

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import org.bitcoinj.core.Address
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

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

    @Unroll
    def "Exact trade match: #amountSPX SPX for #amountSPY SPY"() {
        when:
        def traderA = createFundedAddress(startBTC, zeroAmount, false) // offers SPX
        def traderB = createFundedAddress(startBTC, zeroAmount, false) // offers SPY
        def propertySPX = fundNewProperty(traderA, amountSPX, ecosystem)
        def propertySPY = fundNewProperty(traderB, amountSPY, ecosystem)

        then:
        omniGetBalance(traderA, propertySPY).balance == zeroAmount
        omniGetBalance(traderA, propertySPX).balance == amountSPX.numberValue()
        omniGetBalance(traderB, propertySPY).balance == amountSPY.numberValue()
        omniGetBalance(traderB, propertySPX).balance == zeroAmount

        when: "trader A offers SPX and desired SPY"
        def txidOfferA = omniSendTrade(traderA, propertySPX, amountSPX, propertySPY, amountSPY)
        generateBlock()

        then: "it is a valid open order"
        omniGetTrade(txidOfferA).valid
        omniGetTrade(txidOfferA).status == "open"
        omniGetTrade(txidOfferA).propertyidforsale == propertySPX.getValue()
        omniGetTrade(txidOfferA).propertyiddesired == propertySPY.getValue()
        omniGetTrade(txidOfferA).unitprice == unitPrice

        and: "there is an offering for the new property in the orderbook"
        omniGetOrderbook(propertySPX, propertySPY).size() == 1

        and: "the offered amount is now reserved"
        omniGetBalance(traderA, propertySPX).balance == zeroAmount
        omniGetBalance(traderA, propertySPX).reserved == amountSPX.numberValue()

        when: "trader B offers SPY and desires SPX"
        def txidOfferB = omniSendTrade(traderB, propertySPY, amountSPY, propertySPX, amountSPX)
        generateBlock()

        then: "the order is filled"
        omniGetTrade(txidOfferB).valid
        omniGetTrade(txidOfferB).status == "filled"
        omniGetTrade(txidOfferB).propertyidforsale == propertySPY.getValue()
        omniGetTrade(txidOfferB).propertyiddesired == propertySPX.getValue()
        omniGetTrade(txidOfferB).unitprice == inversePrice

        and: "the offering is no longer listed in the orderbook"
        omniGetOrderbook(propertySPX, propertySPY).size() == 0

        and:
        omniGetBalance(traderA, propertySPY).balance == amountSPY.numberValue()
        omniGetBalance(traderA, propertySPX).balance == zeroAmount
        omniGetBalance(traderB, propertySPY).balance == zeroAmount
        omniGetBalance(traderB, propertySPX).balance == amountSPX.numberValue()

        and:
        omniGetBalance(traderA, propertySPY).reserved == zeroAmount
        omniGetBalance(traderA, propertySPX).reserved == zeroAmount
        omniGetBalance(traderB, propertySPY).reserved == zeroAmount
        omniGetBalance(traderB, propertySPX).reserved == zeroAmount

        where:
        ecosystem      | amountSPX                        | amountSPY                        | unitPrice                                                                | inversePrice
        Ecosystem.MSC  |                    1.indivisible |                    1.indivisible |                   "1.00000000000000000000000000000000000000000000000000" |           "1.00000000000000000000000000000000000000000000000000"
        Ecosystem.TMSC |                    2.indivisible |  9223372036854775807.indivisible | "4611686018427387903.50000000000000000000000000000000000000000000000000" |           "0.00000000000000000021684043449710088682500044719043"
        Ecosystem.MSC  |  9223372036854775807.indivisible |  9223372036854775806.indivisible |                   "0.99999999999999999989157978275144955658749977640478" |           "1.00000000000000000010842021724855044342425516710344"
        Ecosystem.TMSC |                    1.indivisible |           0.00000001.divisible   |                   "0.00000001000000000000000000000000000000000000000000" |   "100000000.00000000000000000000000000000000000000000000000000"
        Ecosystem.MSC  |                    2.indivisible | 92233720368.54775807.divisible   |         "46116860184.27387903500000000000000000000000000000000000000000" |           "0.00000000002168404344971008868250004471904340924471"
        Ecosystem.TMSC |          10000000000.indivisible | 9999999999.99999999.divisible    |                   "0.99999999999999999900000000000000000000000000000000" |           "1.00000000000000000100000000000000000100000000000000"
        Ecosystem.MSC  |           0.00000001.divisible   |                    1.indivisible |           "100000000.00000000000000000000000000000000000000000000000000" |           "0.00000001000000000000000000000000000000000000000000"
        Ecosystem.TMSC | 92233720368.54775807.divisible   |                    2.indivisible |                   "0.00000000002168404344971008868250004471904340924471" | "46116860184.27387903500000000000000000000000000000000000000000"
        Ecosystem.MSC  |  9999999999.99999999.divisible   |          10000000000.indivisible |                   "1.00000000000000000100000000000000000100000000000000" |           "0.99999999999999999900000000000000000000000000000000"
        Ecosystem.TMSC |           0.00000001.divisible   |           0.00000001.divisible   |                   "1.00000000000000000000000000000000000000000000000000" |           "1.00000000000000000000000000000000000000000000000000"
        Ecosystem.MSC  |           0.00000001.divisible   | 92233720368.54775807.divisible   | "9223372036854775807.00000000000000000000000000000000000000000000000000" |           "0.00000000000000000010842021724855044341250022359522"
    }

}
