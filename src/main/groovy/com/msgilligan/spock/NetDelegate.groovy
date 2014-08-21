package com.msgilligan.spock

/**
 * Demonstration delegating trait for testing Groovy traits and delegates with Spock
 */
trait NetDelegate {
    @Delegate NetClient client = new NetClient()
}
