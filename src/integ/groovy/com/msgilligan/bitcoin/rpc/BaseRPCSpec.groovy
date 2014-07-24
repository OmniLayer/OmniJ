package com.msgilligan.bitcoin.rpc

import org.mastercoin.rpc.MastercoinClient
import org.mastercoin.rpc.MastercoinClientDelegate
import spock.lang.Specification

/**
 * User: sean
 * Date: 6/16/14
 * Time: 3:57 PM
 */
abstract class BaseRPCSpec extends Specification implements MastercoinClientDelegate {
    static def rpcproto = "http"
    static def rpchost = "127.0.0.1"
    static def rpcport = 18332
    static def rpcfile = "/"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    static BigDecimal minSatoshisForTest = 5.0
    static BigDecimal testAmount = 2.0

    void setupSpec() {
        // Instantiate a Bitcoin RPC Client
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)

        // Make sure we have enough test coins
        def balance = getBalance(null, null);
        if (balance < minSatoshisForTest) {
            // Mine 101 blocks so we have some coins to spend
            generateBlocks(101)
        }
    }

}
