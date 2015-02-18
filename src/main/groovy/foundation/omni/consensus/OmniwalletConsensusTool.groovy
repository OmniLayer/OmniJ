package foundation.omni.consensus

import groovy.json.JsonSlurper
import foundation.omni.CurrencyID
import org.bitcoinj.core.Address

/**
 * Command-line tool and class for fetching OmniWallet consensus data
 */
class OmniwalletConsensusTool extends ConsensusTool {
    static URI OmniHost_Live = new URI("https://www.omniwallet.org");
//    static URI OmniHost_DBDev = new URI("https://dbdev.omniwallet.org");
    private def proto
    private def host
    private def port
    static String file = "/v1/mastercoin_verify/addresses"
    static String revisionFile = "/v1/system/revision.json"

    OmniwalletConsensusTool(URI hostURI) {
        proto = "https"
        port = 443;
        host = hostURI.host;
    }

    public static void main(String[] args) {
        OmniwalletConsensusTool tool = new OmniwalletConsensusTool(OmniHost_Live)
        tool.run(args.toList())
    }

    private SortedMap<Address, ConsensusEntry> getConsensusForCurrency(URL consensusURL) {
        def slurper = new JsonSlurper()
        def balances = slurper.parse(consensusURL)

        TreeMap<String, ConsensusEntry> map = [:]

        balances.each { item ->

            Address address = new Address(null, item.address)
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
        BigDecimal balanceOut =  new BigDecimal(balanceIn).setScale(8)
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

        /* Since getallbalancesforid_MP doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         *
         * Note: Omni blockheight lags behind Blockchain.info and Master Core and this
         * loop does not resolve that issue, it only makes sure the reported block height
         * matches the data returned.
         */
        Integer beforeBlockHeight = currentBlockHeight()
        Integer curBlockHeight
        SortedMap<Address, ConsensusEntry> entries
        while (true) {
            entries = this.getConsensusForCurrency(consensusURL)
            curBlockHeight = currentBlockHeight()
            if (curBlockHeight == beforeBlockHeight) {
                // If blockHeight didn't change, we're done
                break;
            }
            // Otherwise we have to try again
            beforeBlockHeight = curBlockHeight
        }
        def snap = new ConsensusSnapshot(currencyID, curBlockHeight, "Omniwallet (Master tools)", consensusURL.toURI(), entries);
        return snap
    }
}
