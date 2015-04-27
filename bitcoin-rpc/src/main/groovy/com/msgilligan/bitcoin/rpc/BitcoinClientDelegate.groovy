package com.msgilligan.bitcoin.rpc

/**
 * Trait to Mix-in BitcoinClient methods via Delegation pattern
 */
trait BitcoinClientDelegate {
    @Delegate
    BitcoinClient client
}