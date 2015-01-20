package foundation.omni.rpc

/**
 * Groovy trait for adding a MastercoinCLIClient delegate to any class
 */
trait MastercoinClientDelegate {
    @Delegate
    MastercoinCLIClient client
}
