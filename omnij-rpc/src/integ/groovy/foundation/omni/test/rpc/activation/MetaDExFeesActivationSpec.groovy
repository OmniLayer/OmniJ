package foundation.omni.test.rpc.activation

import foundation.omni.Ecosystem
import org.bitcoinj.core.Address
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Specification for the activation of the fee distribution on the distributed token exchange.
 *
 * Once the feature with identifier 9 is active, fees from trading non-Omni pairs are distributed to Omni holders.
 *
 * During the grace period, the old rules are still in place.
 *
 * Note: this test is only successful with a clean state, and requires that the feature is initially disabled!
 */
@Stepwise
class MetaDExFeesActivationSpec extends BaseActivationSpec {

    @Shared Integer activationBlock = 999999
    @Shared Address actorAddress

    def beforeSpec() {
        skipIfActivated(metaDExFeesFeatureId)
        skipIfVersionOlderThan(1100000)
    }

    def setupSpec() {
        actorAddress = createFundedAddress(startBTC, zeroAmount)
    }

    def "Feature identifier 9 can be used to schedule the activation of the fee distribution"() {
        setup:
        activationBlock = getBlockCount() + activationMinBlocks + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, metaDExFeesFeatureId, activationBlock, minClientVersion)
        generateBlock()

        then:
        omniGetTransaction(txid).valid

        when:
        def activations = omniGetActivations()
        def pendingActivations = activations.pendingactivations
        def completedActivations = activations.completedactivations

        then:
        pendingActivations.any { it.featureid == metaDExFeesFeatureId }
        !completedActivations.any { it.featureid == metaDExFeesFeatureId }

        when:
        def pendingFeature = pendingActivations.find { it.featureid == metaDExFeesFeatureId } as Map<String, Object>

        then:
        pendingFeature.featureid == metaDExFeesFeatureId
        pendingFeature.activationblock == activationBlock
        pendingFeature.minimumversion == minClientVersion
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
        !pendingActivations.any { it.featureid == metaDExFeesFeatureId }
        completedActivations.any { it.featureid == metaDExFeesFeatureId }

        when:
        def activatedFeature = completedActivations.find { it.featureid == metaDExFeesFeatureId } as Map<String, Object>

        then:
        activatedFeature.featureid == metaDExFeesFeatureId
        activatedFeature.activationblock == activationBlock
        activatedFeature.minimumversion == minClientVersion
    }

    def "Omni token holders are qualified to receive a share of trading fees"() {
        setup:
        def actorA = createFundedAddress(startBTC, 50.divisible)
        def actorB = createFundedAddress(startBTC, 100.divisible)
        def actorC = createFundedAddress(startBTC, 150.divisible)
        def actorD = createFundedAddress(startBTC, 200.divisible)
        def actorE = createFundedAddress(startBTC, 500.divisible)

        when:
        def feeShare = omniGetFeeShare(null, Ecosystem.TOMNI)
        def feeShareA = feeShare.find { it.address == actorA.toString() } as Map<String, Object>
        def feeShareB = feeShare.find { it.address == actorB.toString() } as Map<String, Object>
        def feeShareC = feeShare.find { it.address == actorC.toString() } as Map<String, Object>
        def feeShareD = feeShare.find { it.address == actorD.toString() } as Map<String, Object>
        def feeShareE = feeShare.find { it.address == actorE.toString() } as Map<String, Object>

        then:
        feeShareA.feeshare == "5.0000%"
        feeShareB.feeshare == "10.0000%"
        feeShareC.feeshare == "15.0000%"
        feeShareD.feeshare == "20.0000%"
        feeShareE.feeshare == "50.0000%"
    }

}
