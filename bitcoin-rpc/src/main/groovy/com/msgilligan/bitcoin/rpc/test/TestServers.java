package com.msgilligan.bitcoin.rpc.test;

/**
 * Credentials to server under test.
 */
public class TestServers {
    private static final TestServers INSTANCE = new TestServers();
    private final String rpcTestUser = System.getProperty("omni.test.rpcTestUser", "bitcoinrpc");
    private final String rpcTestPassword = System.getProperty("omni.test.rpcTestPassword", "pass");

    public static TestServers getInstance() {
        return INSTANCE;
    }

    public String getRpcTestUser() {
        return rpcTestUser;
    }

    public String getRpcTestPassword() {
        return rpcTestPassword;
    }
}
