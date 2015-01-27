package foundation.omni.consensus

import groovy.json.JsonSlurper
import foundation.omni.CurrencyID

/**
 * Command-line tool and class for fetching Omni Chest consensus data
 */
class ChestConsensusTool extends ConsensusTool {
    // omnichest.info doesn't have https:// support (yet?)
    static URI ChestHost_Live = new URI("http://omnichest.info");
    private def proto
    private def host
    private def port
    static def file = "/mastercoin_verify/addresses.aspx"
    static def blockHeightFile = "/apireq.aspx?stat=customapireq_lastblockprocessed"

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
        BigDecimal balanceOut = new BigDecimal(balanceIn).setScale(8)
        return balanceOut
    }

    private Integer currentBlockHeight() {
        def blockHeightURL = new URL(proto, host, port, blockHeightFile)
        String blockHeight = blockHeightURL.text
        return blockHeight.toInteger()
    }


    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        String httpFile = "${file}?currencyid=${currencyID as Integer}"
        def consensusURL = new URL(proto, host, port, httpFile)

        /* Since getConsensusForCurrency can't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         *
         * Note: Omni blockheight lags behind Blockchain.info and Omni Core and this
         * loop does not resolve that issue, it only makes sure the reported block height
         * matches the data returned.
         */

        Integer beforeBlockHeight = currentBlockHeight()
        Integer curBlockHeight
        SortedMap<String, ConsensusEntry> entries
        while (true) {
            entries = this.getConsensusForCurrency(currencyID)
            curBlockHeight = currentBlockHeight()
            if (curBlockHeight == beforeBlockHeight) {
                // If blockHeight didn't change, we're done
                break;
            }
            // Otherwise we have to try again
            beforeBlockHeight = curBlockHeight
        }

        def snap = new ConsensusSnapshot(currencyID, curBlockHeight, "OmniChest", consensusURL.toURI(), entries);
        return snap
    }
}
