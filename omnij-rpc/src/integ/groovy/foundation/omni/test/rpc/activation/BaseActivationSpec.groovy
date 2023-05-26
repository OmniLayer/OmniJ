package foundation.omni.test.rpc.activation

import foundation.omni.BaseRegTestSpec
import foundation.omni.OmniValue
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Sha256Hash
import org.junit.jupiter.api.Assumptions
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
 * the last 50 blocks is persisted, it completely resets the state, including all activations in-between.
 */
abstract class BaseActivationSpec extends BaseRegTestSpec {

    // Activation grace period
    static final protected Integer activationMinBlocks = 5
    static final protected Integer activationMaxBlocks = 10

    // Feature identifiers
    static final protected Short metaDExFeatureId = 2
    static final protected Short unallocatedFeatureId = 3
    static final protected Short overOffersFeatureId = 5
    static final protected Short allPairDExFeatureId = 8
    static final protected Short metaDExFeesFeatureId = 9

    // Default values
    static protected Integer minClientVersion = 0
    static protected Coin startBTC = 0.1.btc
    static protected OmniValue startMSC = 0.1.divisible
    static protected OmniValue zeroAmount = 0.divisible

    @Shared Sha256Hash initialBlock

    def setupSpec() {
        if (!client.commandExists('omni_sendactivation')) {
            Assumptions.abort('The client has no "omni_sendactivation" command')
        }
        if (!client.commandExists('omni_getactivations')) {
            Assumptions.abort('The client has no "omni_getactivations" command')
        }

        client.generateBlocks(1)
        def currentBlockCount = client.getBlockCount()
        initialBlock = client.getBlockHash(currentBlockCount)
    }

    def cleanupSpec() {
        if (initialBlock == null) {
            return
        }
        for (int i = 0; i < 50; ++i) {
            client.generateBlocks(1)
        }
        client.invalidateBlock(initialBlock)
        client.clearMemPool()
        delayAfterInvalidate()   // Sleep for a few seconds to avoid duplicate block
        client.generateBlocks(1)
    }

    def skipIfActivated(def featureId) {
        def activations = omniGetActivations()
        if (activations.pendingactivations.any( { it.featureid == featureId } )) {
            Assumptions.abort("Feature $featureId is already activated")
        }
        if (activations.completedactivations.any( { it.featureid == featureId } )) {
            Assumptions.abort("Feature $featureId is already live")
        }
    }

    def skipIfVersionOlderThan(def minClientVersion) {
        def clientInfo = client.omniGetInfo()
        def clientVersion = clientInfo.omnicoreversion_int
        if (clientVersion < minClientVersion) {
            Assumptions.abort("Requires at least version $minClientVersion, but is $clientVersion")
        }
    }
}
