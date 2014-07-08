package com.msgilligan.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.MastercoinClient
import groovy.json.JsonSlurper

/**
 * User: sean
 * Date: 7/3/14
 * Time: 12:19 PM
 */
class OmniwalletConsensusFetcher implements ConsensusFetcher {
    static def proto = "https"
    static def host = "www.omniwallet.org"
    static def port = 443
    static def file = "/v1/mastercoin_verify/addresses?currency_id=1"
    URL consensusURL

    OmniwalletConsensusFetcher() {
        consensusURL = new URL(proto, host, port, file)
    }

    public static void main(String[] args) {
        OmniwalletConsensusFetcher fetcher
        Long currencyMSC = 1L

        fetcher = new OmniwalletConsensusFetcher()

        def mscConsensus = fetcher.getConsensusForCurrency(currencyMSC)
        mscConsensus.each {  address, ConsensusBalance bal ->
            println "${address}: ${bal.balance}"
        }
    }

    @Override
    Map<String, ConsensusBalance> getConsensusForCurrency(Long currencyID) {
        def slurper = new JsonSlurper()
        def balancesText =  consensusURL.getText()
        def balances = slurper.parse(consensusURL)

        TreeMap<String, ConsensusBalance> map = [:]
        balances.each { item ->
            String address = item.address
            BigDecimal balance = new BigDecimal(item.balance)
            if (address != "" /* && balance != 0 */) {
                map.put(item.address, new ConsensusBalance(address: address, balance: balance))
            }
        }
        return map;
    }
}
