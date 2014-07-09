package com.msgilligan.mastercoin.consensus

import groovy.json.JsonSlurper

/**
 * User: sean
 * Date: 7/8/14
 * Time: 4:29 PM
 */
class ChestConsensusFetcher implements ConsensusFetcher {
    static def proto = "https"
    static def host = "masterchest.info"
    static def port = 443
    static def file = "/mastercoin_verify/addresses.aspx"

    ChestConsensusFetcher() {
    }

    public static void main(String[] args) {
        ChestConsensusFetcher fetcher
        Long currencyMSC = 1L

        fetcher = new ChestConsensusFetcher()
        // TODO: Check Blockcount
        // Scrape this URL: https://masterchest.info/status.aspx
        //
        def mscConsensus = fetcher.getConsensusForCurrency(currencyMSC)
        mscConsensus.each {  address, ConsensusBalance bal ->
            println "${address}: ${bal.balance}"
        }
    }

    @Override
    Map<String, ConsensusBalance> getConsensusForCurrency(Long currencyID) {
        def slurper = new JsonSlurper()
//        def balancesText =  consensusURL.getText()
        String httpFile = "${file}?currencyid=${currencyID}"
        def consensusURL = new URL(proto, host, port, httpFile)
        def balances = slurper.parse(consensusURL)

        TreeMap<String, ConsensusBalance> map = [:]
        balances.each { item ->
            String address = item.address
            BigDecimal balance = new BigDecimal(item.balance).setScale(8)
            if (address != "") {
                map.put(item.address, new ConsensusBalance(address: address, balance: balance))
            }
        }
        return map;
    }
}
