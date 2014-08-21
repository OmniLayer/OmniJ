package org.mastercoin.rpc

/**
 * Groovy trait for adding a MastercoinClient delegate to any class
 */
trait MastercoinClientDelegate {
    @Delegate
    MastercoinClient client
}
