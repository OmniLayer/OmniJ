package com.msgilligan.bitcoinj.rpc;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class with default connection information for Bitcoin JSON-RPC
 */
public class RPCURI {
    public static final String rpcproto = "http";
    public static final String rpcssl = "https";
    public static final String rpchost = "127.0.0.1";
    public static final String rpcfile = "/";

    public static final int RPCPORT_MAINNET = 8332;
    public static final int RPCPORT_TESTNET = 18332;
    public static final int RPCPORT_REGTEST = 18332;

    public static URI getDefaultMainNetURI() {
        try {
            return new URI(rpcproto, null, rpchost, RPCPORT_MAINNET, rpcfile, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static URI getDefaultTestNetURI() {
        try {
            return new URI(rpcproto, null, rpchost, RPCPORT_TESTNET, rpcfile, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static URI getDefaultRegTestURI() {
        try {
            return new URI(rpcproto, null, rpchost, RPCPORT_REGTEST, rpcfile, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

}
