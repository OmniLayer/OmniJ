package com.msgilligan.bitcoin.rpc;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: sean
 * Date: 7/26/14
 * Time: 7:15 PM
 */
public class RPCURL {
    public static final String rpcproto = "http";
    public static final String rpchost = "127.0.0.1";
    public static final String rpcfile = "/";

    public static final int RPCPORT_MAINNET = 8332;
    public static final int RPCPORT_TESTNET = 18332;
    public static final int RPCPORT_REGTEST = 18332;

    public static URL getDefaultMainNetURL() {
        try {
            return new URL(rpcproto, rpchost, RPCPORT_MAINNET, rpcfile);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URL getDefaultTestNetURL() {
        try {
            return new URL(rpcproto, rpchost, RPCPORT_TESTNET, rpcfile);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URL getDefaultRegTestURL() {
        try {
            return new URL(rpcproto, rpchost, RPCPORT_REGTEST, rpcfile);
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
