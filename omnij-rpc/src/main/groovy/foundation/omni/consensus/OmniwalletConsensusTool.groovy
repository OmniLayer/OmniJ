package foundation.omni.consensus

import foundation.omni.rpc.SmartPropertyListInfo
import groovy.json.JsonSlurper
import foundation.omni.CurrencyID
import groovy.transform.TypeChecked
import org.bitcoinj.core.Address

/**
 * Command-line tool and class for fetching OmniWallet consensus data
 */
class OmniwalletConsensusTool extends ConsensusTool {
    static URI OmniHost_Live = new URI("https://www.omniwallet.org");
    private String proto
    private String host
    private int port
    static String file = "/v1/mastercoin_verify/addresses"
    static String revisionFile = "/v1/system/revision.json"
    static String listFile = "/v1/mastercoin_verify/properties"

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
        BigDecimal reserved = jsonToBigDecimal(item.reserved_balance)
        return new ConsensusEntry(balance: balance, reserved:reserved)
    }

    /* We're expecting input type String here */
    private BigDecimal jsonToBigDecimal(Object balanceIn) {
        BigDecimal balanceOut =  new BigDecimal(balanceIn).setScale(8)
        return balanceOut
    }

    @Override
    Integer currentBlockHeight() {
        def revisionURL = new URL(proto, host, port, revisionFile)
        def slurper = new JsonSlurper()
        def revisionInfo = slurper.parse(revisionURL)
        return revisionInfo.last_block
    }

    /**
     * Only returns Omni Properties, filters out BTC and Fiat (USD, etc) currencies
     * @return
     */
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
        String httpFile = "${file}?currency_id=${currencyID.longValue()}"
        def consensusURL = new URL(proto, host, port, httpFile)

        /* Since getConsensusForCurrency() doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         *
         * Note: Omniwallet blockheight can lag behind Blockchain.info and Omni Core and this
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
        def snap = new ConsensusSnapshot(currencyID, curBlockHeight, "Omniwallet", consensusURL.toURI(), entries);
        return snap
    }
}
