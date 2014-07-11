package com.msgilligan.mastercoin.consensus

import groovy.json.JsonSlurper

/**
 * User: sean
 * Date: 7/8/14
 * Time: 4:29 PM
 */
class ChestConsensusTool extends ConsensusTool {
    static def proto = "https"
    static def host = "masterchest.info"
    static def port = 443
    static def file = "/mastercoin_verify/addresses.aspx"

    ChestConsensusTool() {
    }

    public static void main(String[] args) {
        ChestConsensusTool tool = new ChestConsensusTool()
        tool.run(args.toList())
    }

    private SortedMap<String, ConsensusEntry> getConsensusForCurrency(Long currencyID) {
        def slurper = new JsonSlurper()
//        def balancesText =  consensusURL.getText()
        String httpFile = "${file}?currencyid=${currencyID}"
        def consensusURL = new URL(proto, host, port, httpFile)
        def balances = slurper.parse(consensusURL)

        TreeMap<String, ConsensusEntry> map = [:]
        balances.each { item ->
            String address = item.address
            BigDecimal balance = new BigDecimal(item.balance).setScale(8)
            if (address != "") {
                map.put(item.address, new ConsensusEntry(address: address, balance: balance))
            }
        }
        return map;
    }

    public ConsensusSnapshot getConsensusSnapshot(Long currencyID) {
        String httpFile = "${file}?currencyid=${currencyID}"
        def consensusURL = new URL(proto, host, port, httpFile)

        def snap = new ConsensusSnapshot();
        snap.currencyID = currencyID
        snap.blockHeight = -1
        snap.sourceType = "MasterChest"
        snap.sourceURL = consensusURL
        snap.entries = this.getConsensusForCurrency(currencyID)
        return snap
    }
}
