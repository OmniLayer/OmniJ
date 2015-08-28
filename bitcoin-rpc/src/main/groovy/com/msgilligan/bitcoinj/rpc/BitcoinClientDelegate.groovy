package com.msgilligan.bitcoinj.rpc

/**
 * Trait to Mix-in BitcoinClient methods via Delegation pattern
 */
trait BitcoinClientDelegate {
    @Delegate
    BitcoinClient client
}