package com.msgilligan.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.MastercoinClient
import groovy.json.JsonSlurper

/**
 * User: sean
 * Date: 7/3/14
 * Time: 12:19 PM
 */
class OmniwalletConsensusFetcher implements ConsensusFetcher {
    static def rpcproto = "https"
    static def rpchost = "www.omniwallet.org"
    static def rpcport = 443
    static def rpcfile = "/v1/mastercoin_verify/addresses?currency_id=1"
    static def rpcuser = "bitcoinrpc"
    static def rpcpassword = "pass"
    URL consensusURL

    OmniwalletConsensusFetcher() {
        consensusURL = new URL(rpcproto, rpchost, rpcport, rpcfile)
    }

    @Override
    Map<String, Object> getConsensusForCurrency(Long currencyID) {
        def slurper = new JsonSlurper()
        def balancesText =  consensusURL.getText()
        def balances = slurper.parse(consensusURL)

        Map<String, Object> map = [:]
        balances.each { item ->
            def balance = new ConsensusBalance(address:item.address, balance:item.balance.toBigDecimal())
            map.put(item.address, balance)
        }
        return map;
    }
}
