package foundation.omni.rpc

/**
 * Groovy trait for adding a OmniCLIClient delegate to any class
 */
trait OmniClientDelegate {
    @Delegate
    OmniCLIClient client
}
