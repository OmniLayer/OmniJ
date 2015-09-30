package foundation.omni.test.rpc.activation

import foundation.omni.BaseRegTestSpec
import foundation.omni.OmniValue
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Sha256Hash
import org.junit.internal.AssumptionViolatedException
import spock.lang.Shared

/**
 * Base specification for feature activations in regtest mode
 *
 * The use of activation commands is restricted to whitelisted senders. To whitelist a source to allow feature
 * activations, the option {@code -omniactivationallowsender="sender"} can be used.
 *
 * To avoid that the activations affect other tests, a workaround is used to revert consensus affecting changes:
 *
 * Before the tests, the hash of a "marker block" is stored, and after the tests 50 blocks are mined, and
 * finally the "marker block" is invalidated. This results in a reorganization, and because only the state of
 * the last 50 blocks is persisted, it completely resets the state, including all activations inbetween.
 */
abstract class BaseActivationSpec extends BaseRegTestSpec {

    // Activation grace period
    static final protected Integer activationMinBlocks = 5
    static final protected Integer activationMaxBlocks = 10

    // Feature identifiers
    static final protected Short metaDExFeatureId = 2
    static final protected Short unallocatedFeatureId = 3
    static final protected Short overOffersFeatureId = 5

    // Default values
    static protected Integer minClientVersion = 0
    static protected Coin startBTC = 0.1.btc
    static protected OmniValue startMSC = 0.1.divisible
    static protected OmniValue zeroAmount = 0.divisible

    @Shared Sha256Hash initialBlock

    def setupSpec() {
        if (!commandExists('omni_sendactivation')) {
            throw new AssumptionViolatedException('The client has no "omni_sendactivation" command')
        }
        if (!commandExists('omni_getactivations')) {
            throw new AssumptionViolatedException('The client has no "omni_getactivations" command')
        }

        generateBlock()
        def currentBlockCount = getBlockCount()
        initialBlock = getBlockHash(currentBlockCount)
    }

    def cleanupSpec() {
        if (initialBlock == null) {
            return
        }
        for (int i = 0; i < 50; ++i) {
            generateBlock()
        }
        invalidateBlock(initialBlock)
        clearMemPool()
        generateBlock()
    }

    def skipIfActivated(def featureId) {
        def activations = omniGetActivations()
        if (activations.pendingactivations.any( { it.featureid == featureId } )) {
            throw new AssumptionViolatedException("Feature $featureId is already activated")
        }
        if (activations.completedactivations.any( { it.featureid == featureId } )) {
            throw new AssumptionViolatedException("Feature $featureId is already live")
        }
    }
}
