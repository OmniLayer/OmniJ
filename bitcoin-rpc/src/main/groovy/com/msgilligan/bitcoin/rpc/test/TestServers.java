package com.msgilligan.bitcoin.rpc.test;

/**
 * Credentials to server under test.
 */
public class TestServers {
    public static final String rpcTestUser = System.getProperty("omni.test.rpcTestUser", "bitcoinrpc");
    public static final String rpcTestPassword = System.getProperty("omni.test.rpcTestPassword", "pass");
}
