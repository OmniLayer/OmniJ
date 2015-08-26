package foundation.omni.test.rpc.activation

import foundation.omni.BaseRegTestSpec
import org.junit.internal.AssumptionViolatedException
import spock.lang.Unroll

/**
 * Specification for the grace period of feature activations.
 *
 * Features are activated at specific block heights, which must be within the range of a grace period to ensure
 * users have enough time to update their clients.
 *
 * The use of activation commands is restricted to whitelisted senders. To whitelist a source to allow feature
 * activations, the option {@code -omniactivationallowsender="sender"} can be used.
 *
 * The feature identifier 3 is currently unused, and a good candidate for tests.
 *
 * Note: this test is only successful with a clean state!
 */
class GracePeriodSpec extends BaseRegTestSpec {

    final static Integer minClientVersion = 0
    final static Integer activationMinBlocks = 5
    final static Integer activationMaxBlocks = 10
    final static Short featureId = 3
    final static BigDecimal startBTC = 0.001
    final static BigDecimal zeroAmount = 0.0

    @Unroll
    def "A relative activation height of #blockOffset blocks is smaller than the grace period and not allowed"() {
        setup:
        def actorAddress = createFundedAddress(startBTC, zeroAmount)
        def activationBlock = getBlockCount() + blockOffset + 1 // one extra, for transaction confirmation

        when:
        def txid = omniSendActivation(actorAddress, featureId, activationBlock, minClientVersion)
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
        def txid = omniSendActivation(actorAddress, featureId, activationBlock, minClientVersion)
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
        def txid = omniSendActivation(actorAddress, featureId, activationBlock, minClientVersion)
        generateBlock()

        then:
        omniGetTransaction(txid).valid == true

        where:
        blockOffset << [activationMinBlocks, activationMinBlocks + 1, activationMaxBlocks - 1, activationMaxBlocks]
    }

    def setupSpec() {
        if (!commandExists('omni_sendactivation')) {
            throw new AssumptionViolatedException('The client has no "omni_sendactivation" command')
        }
    }
}
