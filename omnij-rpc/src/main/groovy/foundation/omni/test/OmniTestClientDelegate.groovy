package foundation.omni.test

import foundation.omni.netapi.omnicore.RxOmniTestClient

/**
 * Groovy trait for adding an OmniTestClient delegate to any class
 */
trait OmniTestClientDelegate {
    @Delegate
    RxOmniTestClient client
}