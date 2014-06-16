package com.msgilligan.bitcoin.rpc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: sean
 * Date: 6/15/14
 * Time: 11:21 AM
 */
public class CliTool {
    static final String rpcproto = "http";
    static final String rpchost = "127.0.0.1";
    static final int rpcport = 28332;
    static final String rpcfile = "/";
    static final String rpcuser ="bitcoinrpc";
    static final String rpcpassword ="pass";
    protected BitcoinClient client;

    public CliTool(String[] args) {
        URL rpcServerURL = null;
        try {
            rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("Connecting to: " + rpcServerURL);
        try {
            client = new BitcoinClient(rpcServerURL, rpcuser, rpcpassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
