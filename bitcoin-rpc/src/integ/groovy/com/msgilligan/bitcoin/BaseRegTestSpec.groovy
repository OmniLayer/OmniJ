package com.msgilligan.bitcoin

import com.msgilligan.bitcoin.rpc.BitcoinClient
import com.msgilligan.bitcoin.rpc.Loggable
import com.msgilligan.bitcoin.rpc.RPCURI
import com.msgilligan.bitcoin.test.BTCTestSupport
import spock.lang.Specification
import com.msgilligan.bitcoin.rpc.test.TestServers


/**
 * Abstract Base class for Spock tests of Bitcoin Core in RegTest mode
 */
abstract class BaseRegTestSpec extends Specification implements BTCTestSupport, Loggable {
    static final BigDecimal minBTCForTests = 50.0
    static final private TestServers testServers = TestServers.instance
    static final protected String rpcTestUser = testServers.rpcTestUser
    static final protected String rpcTestPassword = testServers.rpcTestPassword;

    {
        client = new BitcoinClient(RPCURI.defaultRegTestURI, rpcTestUser, rpcTestPassword)
    }

    void setupSpec() {
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

    void cleanupSpec() {
        // Spend almost all coins as fee, to sweep dust
        consolidateCoins()
    }

}