package org.mastercoin.consensus

import groovy.json.JsonSlurper
import org.mastercoin.CurrencyID

/**
 * Command-line tool and class for fetching Master Chest consensus data
 */
class ChestConsensusTool extends ConsensusTool {
    // masterchest.info had https, but omnichest.info doesn't have it (yet?)
    static URI ChestHost_Live = new URI("http://omnichest.info");
    private def proto
    private def host
    private def port
    static def file = "/mastercoin_verify/addresses.aspx"

    ChestConsensusTool(URI chestURI) {
        proto = chestURI.scheme
        port = chestURI.port
        host = chestURI.host
    }

    public static void main(String[] args) {
        ChestConsensusTool tool = new ChestConsensusTool(ChestHost_Live)
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
        SortedMap<String, ConsensusEntry> entries = this.getConsensusForCurrency(currencyID)

        def snap = new ConsensusSnapshot(currencyID, -1, "MasterChest", consensusURL.toURI(), entries);
        return snap
    }
}
