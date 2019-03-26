package foundation.omni.consensus

import foundation.omni.CurrencyID
import foundation.omni.PropertyType
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.ConsensusFetcher
import foundation.omni.rpc.ConsensusSnapshot
import foundation.omni.rpc.SmartPropertyListInfo
import groovy.json.JsonSlurper
import groovy.transform.TypeChecked
import org.bitcoinj.core.Address
import org.bitcoinj.core.LegacyAddress

/**
 * Consensus Fetcher using Omniwallet REST API
 * Factored out of OmniwalletConsensusTool and likely to be rewritten in pure Java
 */
class OmniwalletConsensusFetcher implements ConsensusFetcher {
    static URI OmniHost_Live = new URI("https://www.omniwallet.org");
    static String file = "/v1/mastercoin_verify/addresses"
    static String revisionFile = "/v1/system/revision.json"
    static String listFile = "/v1/mastercoin_verify/properties"
    static String propertyDetailsFile = "/v1/property"         // + /<id>.json

    static final Map owExtraHeaders = ['User-Agent': 'OmniJ/OmniwalletConsensusFetcher']
    static final Map owParameters = [requestProperties: owExtraHeaders]

    private final String proto
    private final String host
    private final int port

    @TypeChecked
    OmniwalletConsensusFetcher () {
        this(OmniHost_Live);
    }

    @TypeChecked
    OmniwalletConsensusFetcher (URI hostURI) {
        proto = "https"
        port = 443;
        host = hostURI.host;
    }

    @Override
    Integer currentBlockHeight() {
        def revisionURL = new URL(proto, host, port, revisionFile)
        def slurper = new JsonSlurper()
        def revisionInfo = slurper.parse(revisionURL, owParameters)
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
        List<Map<String, Object>> props = (List<Map<String, Object>>) slurper.parse(listPropUrl, owParameters)
        List<SmartPropertyListInfo> propList = new ArrayList<SmartPropertyListInfo>()
        for (Map jsonProp : props) {
            // TODO: Should this mapping be done by Jackson?
            String protocol = (String) jsonProp.get("Protocol")
            // Note: Omniwallet also returns currencies with Protocol of "Fiat"
            if (protocol == "Omni") {
                Number idnum = (Number) jsonProp.get("currencyID")
                CurrencyID id
                try {
                    id = new CurrencyID(idnum.longValue())
                } catch (NumberFormatException e) {
                    id = null
                }
                if (id != null && id != CurrencyID.BTC) {
                    String name = (String) jsonProp.get("name")
                    String category = ""
                    String subCategory = ""
                    String data = ""
                    String url = ""
                    Boolean divisible = (Boolean) jsonProp.get("divisible")
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
        }

        return propList
    }

    @Override
    @TypeChecked
    public ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID) {
        /* Since getConsensusForCurrency() doesn't return the blockHeight, we have to check
         * blockHeight before and after the call to make sure it didn't change.
         *
         * Note: Omniwallet blockheight can lag behind Blockchain.info and Omni Core and this
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
        def snap = new ConsensusSnapshot(currencyID, (Long) curBlockHeight, "Omniwallet", consensusURL(currencyID).toURI(), entries);
        return snap
    }

    public SortedMap<Address, BalanceEntry> getConsensusForCurrency(CurrencyID currencyID) {
        PropertyType propertyType = getPropertyType(currencyID)
        def balances = new JsonSlurper().parse(consensusURL(currencyID), owParameters)

        TreeMap<Address, BalanceEntry> map = [:]

        balances.each { item ->

            Address address = LegacyAddress.fromBase58(null, (String) item.address)
            BalanceEntry entry = itemToEntry(propertyType, item)

            if (address != "" && (entry.balance.numberValue() > 0 || entry.reserved.numberValue() > 0)) {
                map.put(address, entry)
            }
        }
        return map;
    }

    protected BalanceEntry itemToEntry(PropertyType propertyType, Object item) {
        BigDecimal balance = jsonToBigDecimal(item.balance)
        BigDecimal reserved = jsonToBigDecimal(item.reserved_balance)
        // TODO: Add `frozen` to the test
        if (propertyType == PropertyType.DIVISIBLE) {
            return new BalanceEntry(balance.divisible, reserved.divisible, 0.divisible)
        } else {
            return new BalanceEntry(balance.indivisible, reserved.indivisible, 0.indivisible)
        }
    }

    /* We're expecting input type String here */
    @TypeChecked
    protected BigDecimal jsonToBigDecimal(String balanceIn) {
        BigDecimal balanceOut =  new BigDecimal(balanceIn).setScale(8)
        return balanceOut
    }

    protected PropertyType getPropertyType(CurrencyID currencyID) {
        if ((currencyID == CurrencyID.OMNI) || (currencyID == CurrencyID.TOMNI)) {
            return PropertyType.DIVISIBLE
        }
        def details = new JsonSlurper().parse(new URL(proto, host, port, "${propertyDetailsFile}/${currencyID.value}.json"), owParameters)
        int type = Integer.parseInt(details[0].propertyType)
        return (type == 1) ? PropertyType.INDIVISIBLE : PropertyType.DIVISIBLE
    }

    @TypeChecked
    protected URL consensusURL(CurrencyID currencyID) {
        return new URL(proto, host, port, "${file}?currency_id=${currencyID.getValue()}")
    }

}
