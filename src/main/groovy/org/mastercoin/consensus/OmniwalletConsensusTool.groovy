package org.mastercoin.consensus

import groovy.json.JsonSlurper

/**
 * User: sean
 * Date: 7/3/14
 * Time: 12:19 PM
 */
class OmniwalletConsensusTool extends ConsensusTool {
    static def proto = "https"
    static def host = "www.omniwallet.org"
    static def port = 443
    static def file = "/v1/mastercoin_verify/addresses"

    OmniwalletConsensusTool() {
    }

    public static void main(String[] args) {
        OmniwalletConsensusTool tool = new OmniwalletConsensusTool()
        tool.run(args.toList())
    }

    private SortedMap<String, ConsensusEntry> getConsensusForCurrency(Long currencyID) {
        def slurper = new JsonSlurper()
//        def balancesText =  consensusURL.getText()
        String httpFile = "${file}?currency_id=${currencyID}"
        def consensusURL = new URL(proto, host, port, httpFile)
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
        BigDecimal reserved = jsonToBigDecimal(item.reserved_balance)
        return new ConsensusEntry(balance: balance, reserved:reserved)
    }

    /* We're expecting input type String here */
    private BigDecimal jsonToBigDecimal(Object balanceIn) {
        BigDecimal balanceOut =  new BigDecimal(balanceIn).setScale(12)
        return balanceOut
    }

    public ConsensusSnapshot getConsensusSnapshot(Long currencyID) {
        String httpFile = "${file}?currency_id=${currencyID}"
        def consensusURL = new URL(proto, host, port, httpFile)

        def snap = new ConsensusSnapshot();
        snap.currencyID = currencyID
        snap.blockHeight = -1
        snap.sourceType = "Omniwallet (Master tools)"
        snap.sourceURL = consensusURL
        snap.entries = this.getConsensusForCurrency(currencyID)
        return snap
    }
}
