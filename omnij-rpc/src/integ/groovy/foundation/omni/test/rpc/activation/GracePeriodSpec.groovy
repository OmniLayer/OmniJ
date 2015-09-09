package foundation.omni.test.rpc.activation

import spock.lang.Unroll

/**
 * Specification for the grace period of feature activations.
 *
 * Features are activated at specific block heights, which must be within the range of a grace period to ensure
 * users have enough time to update their clients.
 *
 * The feature identifier 3 is currently unused, and a good candidate for tests.
 */
class GracePeriodSpec extends BaseActivationSpec {

    @Unroll
    def "A relative activation height of #blockOffset blocks is smaller than the grace period and not allowed"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, zeroAmount)
        def activationBlock = getBlockCount() + blockOffset + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, unallocatedFeatureId, activationBlock, minClientVersion)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        where:
        blockOffset << [-100, 0, 1, 2, 4]
    }

    @Unroll
    def "A relative activation height of #blockOffset blocks is too far in the future and not allowed"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, zeroAmount)
        def activationBlock = getBlockCount() + blockOffset + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, unallocatedFeatureId, activationBlock, minClientVersion)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == false

        where:
        blockOffset << [11, 288, 12289, 999999]
    }

    @Unroll
    def "A relative activation height of #blockOffset blocks is within the grace period and accepted"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, zeroAmount)
        def activationBlock = getBlockCount() + blockOffset + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, unallocatedFeatureId, activationBlock, minClientVersion)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == true

        where:
        blockOffset << [activationMinBlocks, activationMinBlocks + 1, activationMaxBlocks - 1, activationMaxBlocks]
    }
}
