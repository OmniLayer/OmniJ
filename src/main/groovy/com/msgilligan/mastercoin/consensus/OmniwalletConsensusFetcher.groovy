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
    static def file = "/v1/mastercoin_verify/addresses"

    OmniwalletConsensusFetcher() {
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
//        def balancesText =  consensusURL.getText()
        String httpFile = "${file}?currency_id=${currencyID}"
        def consensusURL = new URL(proto, host, port, httpFile)
        def balances = slurper.parse(consensusURL)

        TreeMap<String, ConsensusBalance> map = [:]
        balances.each { item ->
            String address = item.address
            BigDecimal balance = new BigDecimal(item.balance).setScale(8)
            BigDecimal reserved_balance = new BigDecimal(item.reserved_balance).setScale(8)
            if (address != "") {
                map.put(item.address, new ConsensusBalance(address: address, balance: balance, reserved:reserved_balance))
            }
        }
        return map;
    }
}
