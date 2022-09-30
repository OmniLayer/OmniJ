package foundation.omni.rpc

/**
 * Groovy trait for adding a OmniClient delegate to any class
 * @deprecated Use {@code @Delegate OmniClient client} directly
 */
@Deprecated
trait OmniClientDelegate {
    @Delegate
    OmniClient client
}
