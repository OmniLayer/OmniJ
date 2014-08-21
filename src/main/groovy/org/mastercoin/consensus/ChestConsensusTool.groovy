package org.mastercoin.consensus

import groovy.json.JsonSlurper
import org.mastercoin.CurrencyID

/**
 * Command-line tool and class for fetching Master Chest consensus data
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

    private SortedMap<String, ConsensusEntry> getConsensusForCurrency(CurrencyID currencyID) {
        def slurper = new JsonSlurper()
        String httpFile = "${file}?currencyid=${currencyID as Integer}"
        def consensusURL = new URL(proto, host, port, httpFile)
//        def balancesText =  consensusURL.getText()
        def balances = slurper.parse(consensusURL)

        TreeMap<String, ConsensusEntry> map = [:]
        balances.each { item ->

            String address = item.address
            ConsensusEntry entry = itemToEntry(item)

            if (address != "" && entry.balance > 0) {
                map.put(address, entry)
            }
        }
        return map;
    }

    private ConsensusEntry itemToEntry(Object item) {
        BigDecimal balance = jsonToBigDecimal(item.balance)
        BigDecimal reserved = jsonToBigDecimal("0")
        return new ConsensusEntry(balance: balance, reserved:reserved)
    }

    /* We're expecting input type String here */
    private BigDecimal jsonToBigDecimal(Object balanceIn) {
        BigDecimal balanceOut = new BigDecimal(balanceIn).setScale(12)
        return balanceOut
    }

    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        String httpFile = "${file}?currencyid=${currencyID as Integer}"
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
