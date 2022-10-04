package foundation.omni.test

import foundation.omni.rpc.test.OmniTestClient

/**
 * Groovy trait for adding an OmniTestClient delegate to any class
 * @deprecated Use {@code @Delegate OmniTestClient client} directly
 */
@Deprecated
trait OmniTestClientDelegate {
    @Delegate
    OmniTestClient client
}