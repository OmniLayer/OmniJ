package foundation.omni.rpc

import groovy.transform.CompileStatic

/**
 * Groovy trait for adding a MastercoinCLIClient delegate to any class
 */
@CompileStatic
trait MastercoinClientDelegate {
    @Delegate
    MastercoinCLIClient client
}
