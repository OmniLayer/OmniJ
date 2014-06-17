package com.msgilligan.bitcoin.rpc

import spock.lang.Shared
import spock.lang.Specification

/**
 * User: sean
 * Date: 6/16/14
 * Time: 3:57 PM
 */
abstract class BaseRPCSpec extends Specification {
    static def rpcproto = "http"
    static def rpchost = "127.0.0.1"
    static def rpcport = 28332
    static def rpcfile = "/"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    static BigDecimal minSatoshisForTest = 5.0
    static BigDecimal testAmount = 2.0

    @Shared
    MastercoinClient client;

    void setupSpec() {
        // Instantiate a Bitcoin RPC Client
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)

        // Make sure we have enough test coins
        def balance = client.getBalance(null, null);
        if (balance > minSatoshisForTest) {
            client.setGenerate(true, 101)
        }
    }

}
