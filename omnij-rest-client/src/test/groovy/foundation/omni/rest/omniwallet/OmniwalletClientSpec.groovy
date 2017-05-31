package foundation.omni.rest.omniwallet

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.SmartPropertyListInfo
import okhttp3.HttpUrl
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Unroll

import static foundation.omni.CurrencyID.*
import foundation.omni.net.OmniMainNetParams
import org.bitcoinj.core.Address
import spock.lang.Specification


/**
 * Functional (Integration) test of OmniwalletClient
 */
@Ignore("This is really an integration test")
class OmniwalletClientSpec extends Specification {
    final Address exodusAddress = OmniMainNetParams.get().exodusAddress;
    final Address testAddr = new Address(null, "19ZbcHED8F6u5Wr5gp97KMVNvKV8HUrmeu")

    @Shared OmniwalletClient client

    def "get block height" () {
        when:
        def height = client.currentBlockHeight()

        then: "height is a reasonable MainNet block height"
        height > 400000
    }
    
    def "load balances of addresses with single address"() {
        when:
        def balances = client.balancesForAddresses([testAddr])

        then:
        balances != null
        balances[testAddr][USDT].numberValue() >= 0
        balances[testAddr][BTC].numberValue() >= 0
    }

    def "load balances of addresses with multiple addresses"() {
        when:
        def balances = client.balancesForAddresses([testAddr, exodusAddress])

        then:
        balances != null
        balances[testAddr][USDT].numberValue() >= 0
        balances[testAddr][BTC].numberValue() >= 0
        balances[exodusAddress][OMNI].numberValue() >= 0
        balances[exodusAddress][TOMNI].numberValue() >= 0
        balances[exodusAddress][BTC].numberValue() >= 0
    }

    def "load balances of addresses with multiple addresses - in single request"() {
        when:
        // Note: This call is a direct test of th private `service` object
        def balances = client.service.balancesForAddresses([testAddr, exodusAddress]).execute().body()

        then:
        balances != null
    }

    def "Can get Omniwallet property list"() {
        when: "we get data"
        def properties = client.listProperties()

        then: "we get a list of size >= 2"
        properties != null
        properties.size() >= 2

        when: "we convert the list to a map"
        // This may be unnecessary if we can assume the property list is ordered by propertyid
        Map<CurrencyID, SmartPropertyListInfo> props = properties.collect{[it.propertyid, it]}.collectEntries()

        then: "we can check OMNI and TOMNI are as expected"
        props[OMNI].propertyid == OMNI
        props[OMNI].propertyid.ecosystem == Ecosystem.OMNI
        props[OMNI].name == "Omni"
        props[OMNI].category == ""
        props[OMNI].subcategory == ""
        props[OMNI].data == ""
        props[OMNI].url == ""
        props[OMNI].divisible == true

        props[TOMNI].propertyid == TOMNI
        props[TOMNI].propertyid.ecosystem == Ecosystem.TOMNI
        props[TOMNI].name == "Test Omni"
        props[TOMNI].category == ""
        props[TOMNI].subcategory == ""
        props[TOMNI].data == ""
        props[TOMNI].url == ""
        props[TOMNI].divisible == true

        // Assumes MainNet
        props[USDT].propertyid == USDT
        props[USDT].propertyid.ecosystem == Ecosystem.OMNI
        props[USDT].name == "TetherUS"
        props[USDT].category == ""
        props[USDT].subcategory == ""
        props[USDT].data == ""
        props[USDT].url == ""
        props[USDT].divisible == true
    }

    @Unroll
    def "we can get consensus info for currency: #id"(CurrencyID id, SmartPropertyListInfo info) {
        setup:
        def propType = info.divisible ? PropertyType.DIVISIBLE: PropertyType.INDIVISIBLE
        when: "we get data"
        SortedMap<Address, BalanceEntry> balances = client.getConsensusForCurrency(id)

        then: "something is there"
        balances.size() >= 0

        and: "all balances of correct property type"
        balances.every {address, entry ->
            entry.reserved.propertyType == propType
        }
        balances.every {address, entry ->
            entry.balance.propertyType == propType
        }

        where:
        [id, info] << client.listProperties().collect{[it.propertyid, it]}  // Test for ALL currencies on MainNet
    }

    def setup() {
        HttpUrl baseURL = HttpUrl.parse("https://staging.omniwallet.org")
        boolean debug = true
        client = new OmniwalletClient(baseURL, true)
    }
}
