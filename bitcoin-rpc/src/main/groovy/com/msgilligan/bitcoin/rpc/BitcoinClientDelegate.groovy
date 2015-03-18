package com.msgilligan.bitcoin.rpc

/**
 *
 */
trait BitcoinClientDelegate {
    @Delegate
    BitcoinClient client
}