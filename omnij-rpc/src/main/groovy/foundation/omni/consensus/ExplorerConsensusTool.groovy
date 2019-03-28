package foundation.omni.consensus

import foundation.omni.PropertyType
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.ConsensusSnapshot
import foundation.omni.rpc.SmartPropertyListInfo
import groovy.json.JsonSlurper
import foundation.omni.CurrencyID
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.bitcoinj.core.Address
import org.bitcoinj.core.LegacyAddress

/**
 * Command-line tool and class for fetching OmniExplorer consensus data
 */
@Slf4j
class ExplorerConsensusTool implements ConsensusTool {
    static URI ExplorerHost_Live = new URI("https://api.omniexplorer.info");
    private final String proto
    private final String host
    private final int port
    static String file = "/v1/property/distribution/"
    static String listFile = "/v1/properties/list"
    static String revisionFile = "/v1/system/revision.json"

    static final Map apiExtraHeaders = ['User-Agent': 'OmniJ/ExplorerConsensusTool']
    static final Map apiParameters = [requestProperties: apiExtraHeaders]

    ExplorerConsensusTool(URI explorerURI) {
        proto = explorerURI.scheme
        port = explorerURI.port
        host = explorerURI.host
    }

    public static void main(String[] args) {
        ExplorerConsensusTool tool = new ExplorerConsensusTool(ExplorerHost_Live)
        tool.run(args.toList())
    }

    private SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) {
        def balances = new JsonSlurper().parse(consensusURL(currencyID), apiParameters)

        TreeMap<Address, BalanceEntry> map = [:]
        balances.each { item ->

            Address address = LegacyAddress.fromBase58(null, item.address)
            BalanceEntry entry = itemToEntry(item)

            if (address != "" && (entry.balance.numberValue() > 0 || entry.reserved.numberValue() > 0)) {
                map.put(address, entry)
            }
        }
        return map;
    }

    private BalanceEntry itemToEntry(Object item) {
        BigDecimal balance = jsonToBigDecimal(item.balance)
        BigDecimal reserved = jsonToBigDecimal(item.reserved)

        def balanceString = (String) item.balance;
        boolean hasDecimal = balanceString.contains('.') || balanceString.contains('E')
        def propertyType = hasDecimal ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE

        // TODO: Don't hardcode "frozen" to 0 (and add to tests)
        if (propertyType == PropertyType.DIVISIBLE) {
            return new BalanceEntry(balance.divisible, reserved.divisible, 0.divisible)
        } else {

            return new BalanceEntry(balance.indivisible, reserved.indivisible, 0.indivisible)
        }
    }

    /* We're expecting input type String here */
    private BigDecimal jsonToBigDecimal(String balanceIn) {
        BigDecimal balanceOut = new BigDecimal(balanceIn).setScale(8)
        return balanceOut
    }

    @Override
    Integer currentBlockHeight() {
        def revisionURL = new URL(proto, host, port, revisionFile)
        def revision = new JsonSlurper().parseText(revisionURL.getText(apiParameters))
        def blockHeight = revision.last_block
        return blockHeight
    }

    @Override
    @TypeChecked
    List<SmartPropertyListInfo> listProperties() {
        def listPropUrl = new URL(proto, host, port, listFile)
        def slurper = new JsonSlurper()
        Map json = (Map) slurper.parse(listPropUrl, apiParameters)
        List<Map<String, Object>> props = (List<Map<String, Object>>) json.get("properties")
        List<SmartPropertyListInfo> propList = new ArrayList<SmartPropertyListInfo>()
        for (Map<String, Object> jsonProp : props) {
            // TODO: Should this mapping be done by Jackson?
            Number idnum = (Number) jsonProp.get("propertyid")
            CurrencyID id
            try {
                id = new CurrencyID(idnum.longValue())
            } catch (NumberFormatException e) {
                id = null
            }
            if (id != null) {
                String name = (String) jsonProp.get("name")
                String category = (String) jsonProp.get("category")
                String subcategory = (String) jsonProp.get("subcategory")
                String data = (String) jsonProp.get("data")
                String url = (String) jsonProp.get("url")
                Boolean divisible = (Boolean) jsonProp.get("divisible")
                SmartPropertyListInfo prop = new SmartPropertyListInfo(id,
                        name,
                        category,
                        subcategory,
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
        /* Since getConsensusForCurrency can't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         *
         * Note: OmniExplorer blockheight can lag behind Blockchain.info and Omni Core and this
         * loop does not resolve that issue, it only makes sure the reported block height
         * matches the data returned.
         */

        Integer beforeBlockHeight = currentBlockHeight()
        Integer curBlockHeight
        SortedMap<Address, BalanceEntry> entries
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

        def snap = new ConsensusSnapshot(currencyID, curBlockHeight, "OmniExplorer", consensusURL(currencyID).toURI(), entries);
        return snap
    }

    private consensusURL(CurrencyID currencyID) {
        return new URL(proto, host, port, "${file}${currencyID.getValue()}")
    }

}
