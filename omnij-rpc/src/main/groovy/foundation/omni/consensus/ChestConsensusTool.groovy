package foundation.omni.consensus

import foundation.omni.rpc.SmartPropertyListInfo
import groovy.json.JsonSlurper
import foundation.omni.CurrencyID
import groovy.transform.TypeChecked
import org.bitcoinj.core.Address

/**
 * Command-line tool and class for fetching Omni Chest consensus data
 */
class ChestConsensusTool extends ConsensusTool {
    // omnichest.info doesn't have https:// support (yet?)
    static URI ChestHost_Live = new URI("http://omnichest.info");
    private String proto
    private String host
    private int port
    static String file = "/mastercoin_verify/addresses.aspx"
    static String listFile = "/mastercoin_verify/properties.aspx/"
    static String blockHeightFile = "/apireq.aspx?stat=customapireq_lastblockprocessed"

    ChestConsensusTool(URI chestURI) {
        proto = chestURI.scheme
        port = chestURI.port
        host = chestURI.host
    }

    public static void main(String[] args) {
        ChestConsensusTool tool = new ChestConsensusTool(ChestHost_Live)
        tool.run(args.toList())
    }

    private SortedMap<Address, ConsensusEntry> getConsensusForCurrency(CurrencyID currencyID) {
        def slurper = new JsonSlurper()
        String httpFile = "${file}?currencyid=${currencyID as Long}"
        def consensusURL = new URL(proto, host, port, httpFile)
//        def balancesText =  consensusURL.getText()
        def balances = slurper.parse(consensusURL)

        TreeMap<Address, ConsensusEntry> map = [:]
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
        BigDecimal reserved = jsonToBigDecimal(item.reserved)
        return new ConsensusEntry(balance: balance, reserved:reserved)
    }

    /* We're expecting input type String here */
    private BigDecimal jsonToBigDecimal(Object balanceIn) {
        BigDecimal balanceOut = new BigDecimal(balanceIn).setScale(8)
        return balanceOut
    }

    @Override
    Integer currentBlockHeight() {
        def blockHeightURL = new URL(proto, host, port, blockHeightFile)
        String blockHeight = blockHeightURL.text
        return blockHeight.toInteger()
    }

    @Override
    @TypeChecked
    List<SmartPropertyListInfo> listProperties() {
        def listPropUrl = new URL(proto, host, port, listFile)
        def slurper = new JsonSlurper()
        List<Map<String, Object>> props = (List<Map<String, Object>>) slurper.parse(listPropUrl)
        List<SmartPropertyListInfo> propList = new ArrayList<SmartPropertyListInfo>()
        for (Map jsonProp : props) {
            // TODO: Should this mapping be done by Jackson?
            Number idnum = (Number) jsonProp.get("currencyID")
            CurrencyID id
            try {
                id = new CurrencyID(idnum.longValue())
            } catch (NumberFormatException e) {
                id = null
            }
            if (id != null) {
                String name = (String) jsonProp.get("name")
                String category = ""
                String subCategory = ""
                String data = ""
                String url = ""
                Boolean divisible = null
                SmartPropertyListInfo prop = new SmartPropertyListInfo(id,
                        name,
                        category,
                        subCategory,
                        data,
                        url,
                        divisible)
                propList.add(prop)
            }
        }
        return propList
    }

    @Override
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        String httpFile = "${file}?currencyid=${currencyID as Long}"
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
        SortedMap<Address, ConsensusEntry> entries
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
