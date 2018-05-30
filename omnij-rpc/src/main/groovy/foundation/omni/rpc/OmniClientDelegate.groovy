package foundation.omni.rpc

/**
 * Groovy trait for adding a OmniClient delegate to any class
 */
trait OmniClientDelegate {
    @Delegate
    OmniClient client
}
