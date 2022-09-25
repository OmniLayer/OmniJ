package foundation.omni.test

import foundation.omni.rpc.test.OmniTestClient

/**
 * Groovy trait for adding an OmniTestClient delegate to any class
 */
trait OmniTestClientDelegate {
    @Delegate
    OmniTestClient client
}