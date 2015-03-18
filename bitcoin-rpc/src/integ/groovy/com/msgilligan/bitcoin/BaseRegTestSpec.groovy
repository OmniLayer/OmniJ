package com.msgilligan.bitcoin

import com.msgilligan.bitcoin.rpc.BitcoinClient
import com.msgilligan.bitcoin.rpc.BitcoinClientDelegate
import com.msgilligan.bitcoin.rpc.Loggable
import com.msgilligan.bitcoin.rpc.RPCURI
import spock.lang.Specification
import com.msgilligan.bitcoin.rpc.test.TestServers


/**
 *
 */
class BaseRegTestSpec extends Specification implements BitcoinClientDelegate, Loggable {
    static final BigDecimal minBTCForTests = 50.0;

    {
        client = new BitcoinClient(RPCURI.defaultRegTestURI, TestServers.rpcTestUser, TestServers.rpcTestPassword)
    }

    void setupSpec() {
        log.info "Waiting for server..."
        Boolean available = client.waitForServer(60)   // Wait up to 1 minute
        if (!available) {
            log.error "Timeout error."
        }
        assert available

        // Make sure we have enough test coins
        while (getBalance() < minBTCForTests) {
            // Mine blocks until we have some coins to spend
            client.generateBlocks(1)
        }
    }

}