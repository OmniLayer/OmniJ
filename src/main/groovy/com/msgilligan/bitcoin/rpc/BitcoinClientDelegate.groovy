package com.msgilligan.bitcoin.rpc

import groovy.transform.CompileStatic

/**
 * Groovy trait for adding a BitcoinCLIClient delegate to any class
 */
@CompileStatic
trait BitcoinClientDelegate {
    @Delegate
    BitcoinCLIClient client
}
