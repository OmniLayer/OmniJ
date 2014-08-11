package org.mastercoin.consensus

import groovy.json.JsonSlurper
import org.mastercoin.CurrencyID

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
    static def revisionFile = "/v1/system/revision.json"

    OmniwalletConsensusTool() {
    }

    public static void main(String[] args) {
        OmniwalletConsensusTool tool = new OmniwalletConsensusTool()
        tool.run(args.toList())
    }

    private SortedMap<String, ConsensusEntry> getConsensusForCurrency(URL consensusURL) {
        def slurper = new JsonSlurper()
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

    private Integer currentBlockHeight() {
        def revisionURL = new URL(proto, host, port, revisionFile)
        def slurper = new JsonSlurper()
        def revisionInfo = slurper.parse(revisionURL)
        return revisionInfo.last_block
    }

    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        String httpFile = "${file}?currency_id=${currencyID as Integer}"
        def consensusURL = new URL(proto, host, port, httpFile)

        def snap = new ConsensusSnapshot();
        snap.currencyID = currencyID
        snap.sourceType = "Omniwallet (Master tools)"
        snap.sourceURL = consensusURL

        /* Since getallbalancesforid_MP doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         *
         * Note: Omni blockheight lags behind Blockchain.info and Master Core and this
         * loop does not resolve that issue, it only makes sure the reported block height
         * matches the data returned.
         */
        Integer beforeBlockHeight = currentBlockHeight()
        while (true) {
            snap.entries = this.getConsensusForCurrency(consensusURL)
            snap.blockHeight = currentBlockHeight()
            if (snap.blockHeight == beforeBlockHeight) {
                // If blockHeight didn't change, we're done
                break;
            }
            // Otherwise we have to try again
            beforeBlockHeight = snap.blockHeight
        }
        return snap
    }
}
