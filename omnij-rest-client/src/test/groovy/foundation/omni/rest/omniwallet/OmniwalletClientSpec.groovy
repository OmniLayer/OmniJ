package foundation.omni.rest.omniwallet

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.SmartPropertyListInfo
import spock.lang.Shared
import spock.lang.Unroll

import static foundation.omni.CurrencyID.*
import foundation.omni.net.OmniMainNetParams
import org.bitcoinj.core.Address
import spock.lang.Specification


/**
 *
 */
class OmniwalletClientSpec extends Specification {
    final Address exodusAddress = OmniMainNetParams.get().exodusAddress;
    final Address testAddr = new Address(null, "19ZbcHED8F6u5Wr5gp97KMVNvKV8HUrmeu")

    @Shared def client

    def "get block height" () {
        when:
        def height = client.currentBlockHeight()

        then: "height is a reasonable MainNet block height"
        height > 400000
    }

    def "load testAddr balance"() {
        when:
        def balances = client.balancesForAddress(testAddr)

        then:
        balances != null
        balances[0].symbol == "SP31"
        balances[0].id == USDT
        balances[0].value >= 0
        balances[1].symbol == "BTC"
        balances[1].id == BTC
        balances[1].value.numberValue() >= 0
    }

    def "load exodusAddress balance"() {
        when:
        def balances = client.balancesForAddress(exodusAddress)
        balances.sort{a, b -> a.id <=> b.id }

        then:
        balances != null
        balances[0].symbol == "BTC"
        balances[0].id == BTC
        balances[0].value.numberValue() >= 0
        balances[1].symbol == "OMNI"
        balances[1].id == OMNI
        balances[1].value.numberValue() >= 0
        balances[2].symbol == "T-OMNI"
        balances[2].id == TOMNI
        balances[2].value.numberValue() >= 0
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
        props[OMNI].propertyid.ecosystem == Ecosystem.MSC
        props[OMNI].name == "Omni" // Note: Omni Core returns "MasterCoin" with a capital-C
        props[OMNI].category == ""
        props[OMNI].subcategory == ""
        props[OMNI].data == ""
        props[OMNI].url == ""
        props[OMNI].divisible == null

        props[TOMNI].propertyid == TOMNI
        props[TOMNI].propertyid.ecosystem == Ecosystem.TMSC
        props[TOMNI].name == "Test Omni" // Note: Omni Core returns "Mastercoin" with a capital-C
        props[TOMNI].category == ""
        props[TOMNI].subcategory == ""
        props[TOMNI].data == ""
        props[TOMNI].url == ""
        props[TOMNI].divisible == null

        // Assumes MainNet
        props[USDT].propertyid == USDT
        props[USDT].propertyid.ecosystem == Ecosystem.MSC
        props[USDT].name == "TetherUS"
        props[USDT].category == ""
        props[USDT].subcategory == ""
        props[USDT].data == ""
        props[USDT].url == ""
        props[USDT].divisible == null
    }

    @Unroll
    def "we can get consensus info for currency: #currency"() {
        setup:
        def propType = ((currency == MAID) || (currency == SEC)) ?
                        PropertyType.INDIVISIBLE : PropertyType.DIVISIBLE;
        when: "we get data"
        SortedMap<Address, BalanceEntry> balances = client.getConsensusForCurrency(currency)

        then: "something is there"
        balances.size() >= 1

        and: "all balances of correct property type"
        balances.every {address, entry ->
            entry.balance.propertyType == propType && entry.reserved.propertyType == propType
        }

        where:
        currency << [OMNI, TOMNI, MAID, USDT, AGRS, EURT, SEC]
    }

    def setup() {
        client = new OmniwalletClient()
    }
}
