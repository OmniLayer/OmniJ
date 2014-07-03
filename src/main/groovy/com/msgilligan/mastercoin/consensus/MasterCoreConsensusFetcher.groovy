package com.msgilligan.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.MastercoinClient

/**
 * User: sean
 * Date: 7/3/14
 * Time: 11:45 AM
 */
class MasterCoreConsensusFetcher implements ConsensusFetcher {
    static def rpcproto = "http"
    static def rpchost = "127.0.0.1"
    static def rpcport = 8332
    static def rpcfile = "/"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    MastercoinClient client

    MasterCoreConsensusFetcher() {
        def rpcServerURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
        client = new MastercoinClient(rpcServerURL, rpcuser, rpcpassword)
    }

    @Override
    Map<String, Object> getConsensusForCurrency(Long currencyID) {
        def balances = client.getallbalancesforid_MP(currencyID)

        Map<String, Object> map = [:]
        balances.each { item ->
            def balance = new ConsensusBalance(address:item.address, balance:item.balance)
            map.put(item.address, balance)
        }
        return map;
    }
}
