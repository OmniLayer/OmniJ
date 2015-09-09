package foundation.omni.test.rpc.activation

import foundation.omni.BaseRegTestSpec
import org.junit.internal.AssumptionViolatedException

/**
 * Base specification for feature activations in regtest mode
 *
 * The use of activation commands is restricted to whitelisted senders. To whitelist a source to allow feature
 * activations, the option {@code -omniactivationallowsender="sender"} can be used.
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
    static protected BigDecimal startBTC = 0.1
    static protected BigDecimal startMSC = 0.1
    static protected BigDecimal zeroAmount = 0.0

    def setupSpec() {
        if (!commandExists('omni_sendactivation')) {
            throw new AssumptionViolatedException('The client has no "omni_sendactivation" command')
        }
        if (!commandExists('omni_getactivations')) {
            throw new AssumptionViolatedException('The client has no "omni_getactivations" command')
        }
    }
}
