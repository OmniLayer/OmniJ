package foundation.omni.rest.omniwallet

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.OmniValue
import foundation.omni.PropertyType
import foundation.omni.net.OmniMainNetParams
import foundation.omni.json.pojo.OmniJBalances
import foundation.omni.json.pojo.WalletAddressBalance
import foundation.omni.netapi.omniwallet.OmniwalletAbstractClient
import foundation.omni.netapi.omniwallet.json.RevisionInfo
import foundation.omni.BalanceEntry
import foundation.omni.json.pojo.SmartPropertyListInfo
import org.bitcoinj.core.Address
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.Sha256Hash
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static foundation.omni.CurrencyID.*

/**
 * Functional (Integration) test of OmniwalletClient
 */
@Ignore("This is really an integration test")
class OmniwalletClientSpec extends Specification {
    final static Address exodusAddress = OmniMainNetParams.get().exodusAddress;
    final static Address testAddr = LegacyAddress.fromBase58(null, "19ZbcHED8F6u5Wr5gp97KMVNvKV8HUrmeu")

    @Shared OmniwalletAbstractClient client

    def "get revision info" () {
        when:
        RevisionInfo info = client.revisionInfo().get()

        then: "results look reasonable"
        info.getLastBlock() > 400000
        info.getBlockHash() instanceof Sha256Hash
    }

    def "get block height" () {
        when:
        def height = client.currentBlockHeightAsync().get()

        then: "height is a reasonable MainNet block height"
        height > 400000
    }


    def "get block height asynchronously" () {
        when:
        def future = client.currentBlockHeightAsync()
        def height = future.get()

        then: "height is a reasonable MainNet block height"
        height > 400000
    }

    def      "load balances of address with single address"() {
        when:
        WalletAddressBalance balances = client.balancesForAddress(testAddr)

        then:
        balances != null
        balances[USDT].balance.numberValue() >= 0
    }

    def "load balances of address with single address asynchronously"() {
        when:
        WalletAddressBalance balances = client.balancesForAddressAsync(testAddr).get()

        then:
        balances != null
        balances[USDT].balance.numberValue() >= 0

        cleanup:
        Thread.sleep(12_100)    // Don't exceed 5 requests per 60 seconds for this endpoint
    }

    def "load balances of addresses with single address"() {
        when:
        OmniJBalances balances = client.balancesForAddresses([testAddr])

        then:
        balances != null
        balances[testAddr][USDT].balance.numberValue() >= 0

        cleanup:
        Thread.sleep(12_100)    // Don't exceed 5 requests per 60 seconds for this endpoint
    }

    def "load balances of addresses with multiple addresses"() {
        when:
        def balances = client.balancesForAddresses([testAddr, exodusAddress])

        then:
        balances != null
        balances[testAddr][USDT].balance.numberValue() >= 0
        balances[exodusAddress][OMNI].balance.numberValue() >= 0
        balances[exodusAddress][TOMNI].balance.numberValue() >= 0

        cleanup:
        Thread.sleep(12_100)    // Don't exceed 5 requests per 60 seconds for this endpoint
    }

    def "load balances of addresses with multiple addresses asynchronously"() {
        when:
        def future = client.balancesForAddressesAsync([testAddr, exodusAddress])
        def balances = future.get()

        then:
        balances != null
        balances[testAddr][USDT].balance.numberValue() >= 0
        balances[exodusAddress][OMNI].balance.numberValue() >= 0
        balances[exodusAddress][TOMNI].balance.numberValue() >= 0


        cleanup:
        Thread.sleep(12_100)    // Don't exceed 5 requests per 60 seconds for this endpoint
    }

    def "load balances of addresses with multiple addresses - in single request"() {
        when:
        // Note: This call is a direct test of the private Retrofit `service` object
        def balances = (client instanceof OmniwalletClient)
                ? client.service.balancesForAddresses([testAddr, exodusAddress]).get().body()
                : 'dummy'

        then:
        balances != null

        cleanup:
        Thread.sleep(12_100)    // Don't exceed 5 requests per 60 seconds for this endpoint
    }

    def "Can get Omniwallet property list"() {
        when: "we get data"
        def properties = client.listSmartProperties().get()

        then: "we get a list of size >= 2"
        properties != null
        properties.size() >= 2

        when: "we convert the list to a map"
        // This may be unnecessary if we can assume the property list is ordered by propertyid
        Map<CurrencyID, SmartPropertyListInfo> props = properties.collect{[it.propertyid, it]}.collectEntries()

        then: "we can check OMNI and TOMNI are as expected"
        props[OMNI].propertyid == OMNI
        props[OMNI].propertyid.ecosystem == Ecosystem.OMNI
        props[OMNI].name == "Omni Token"
        props[OMNI].category == "N/A"
        props[OMNI].subcategory == "N/A"
        props[OMNI].data == "Omni tokens serve as the binding between Bitcoin, smart properties and contracts created on the Omni Layer."
        props[OMNI].url == "http://www.omnilayer.org"
        props[OMNI].divisible == true

        props[TOMNI].propertyid == TOMNI
        props[TOMNI].propertyid.ecosystem == Ecosystem.TOMNI
        props[TOMNI].name == "Test Omni Token"
        props[TOMNI].category == "N/A"
        props[TOMNI].subcategory == "N/A"
        props[TOMNI].data == "Test Omni tokens serve as the binding between Bitcoin, smart properties and contracts created on the Omni Layer."
        props[TOMNI].url == "http://www.omnilayer.org"
        props[TOMNI].divisible == true

        // Assumes MainNet
        props[USDT].propertyid == USDT
        props[USDT].propertyid.ecosystem == Ecosystem.OMNI
        props[USDT].name == "TetherUS"
        props[USDT].category == "Financial and insurance activities"
        props[USDT].subcategory == "Activities auxiliary to financial service and insurance activities"
        props[USDT].data == "The next paradigm of money."
        props[USDT].url == "https://tether.to"
        props[USDT].divisible == true
    }

    @Ignore
    @Unroll
    def "we can get consensus info for currency: #id"(CurrencyID id, SmartPropertyListInfo info) {
        setup:
        def propType = info.divisible ? PropertyType.DIVISIBLE: PropertyType.INDIVISIBLE
        when: "we get data"
        SortedMap<Address, BalanceEntry> balances = client.getConsensusForCurrency(id)

        then: "something is there"
        balances.size() >= 0

        and: "all balances of correct property type"
        allPropTypesValid(balances, propType)

        and: "all balances are in valid range"
        allBalancesValid(balances)

        where:
        [id, info] << client.listSmartProperties().get().collect{[it.propertyid, it]}  // Test for ALL currencies on MainNet
    }

    @Ignore("USDT just takes too damn long: 504: Gateway time-out")
    def "we can get consensus info for USDT"() {
        setup:
        def propType = PropertyType.DIVISIBLE
        when: "we get data"
        SortedMap<Address, BalanceEntry> balances = client.getConsensusForCurrency(USDT)

        then: "something is there"
        balances.size() >= 0

        and: "all balances of correct property type"
        allPropTypesValid(balances, propType)

        and: "all balances are in valid range"
        allBalancesValid(balances)
    }


    def "we can get consensus info for Mulligan coin (largest indivisible issuance)"() {
        setup:
        def propType = PropertyType.INDIVISIBLE
        when: "we get data"
        SortedMap<Address, BalanceEntry> balances = client.getConsensusForCurrency(CurrencyID.of(340))

        then: "something is there"
        balances.size() >= 0

        and: "all balances of correct property type"
        allPropTypesValid(balances, propType)

        and: "all balances are in valid range"
        allBalancesValid(balances)
    }

    def "we can get consensus info for wbch.xyz coin (large indivisible issuance)"() {
        setup:
        def propType = PropertyType.INDIVISIBLE
        when: "we get data"
        SortedMap<Address, BalanceEntry> balances = client.getConsensusForCurrency(CurrencyID.of(381))

        then: "something is there"
        balances.size() >= 0

        and: "all balances of correct property type"
        allPropTypesValid(balances, propType)

        and: "all balances are in valid range"
        allBalancesValid(balances)
    }

    def "we can get consensus info for SAFEX"() {
        setup:
        def propType = PropertyType.INDIVISIBLE
        when: "we get data"
        SortedMap<Address, BalanceEntry> balances = client.getConsensusForCurrency(SAFEX)

        then: "something is there"
        balances.size() >= 0

        and: "all balances of correct property type"
        allPropTypesValid(balances, propType)

        and: "all balances are in valid range"
        allBalancesValid(balances)
    }

    def "Can get native Omniwallet property list"() {
        when: "we get data"
        def response = client.propertiesList().get()

        then: "we get a list of size >= 2"
        response.propertyInfoList != null
        response.propertyInfoList.size() >= 2

        when: "we look at Tether information"
        def usdtInfo = response.propertyInfoList.stream()
                .filter({it.propertyid == USDT})
                .findFirst().get()
        def totalIssuance = usdtInfo.issuances.stream()
                .map({p -> (p.grant > 0) ? p.grant : -p.revoke })   // Grants are +, revokes are -
                .reduce(0.0, { a,b -> a + b })

        then: "there is some issuance info there"
        usdtInfo.issuances.size() >= 90

        and: "the total of grants and revokes equals totalTokens"
        totalIssuance == usdtInfo.totalTokens
    }

    boolean allPropTypesValid(Map<Address, BalanceEntry> balances, PropertyType expectedPropType) {
        balances.every {address, entry ->
            entry.balance.propertyType == expectedPropType && entry.reserved.propertyType == expectedPropType
        }
    }

    boolean allBalancesValid(Map<Address, BalanceEntry> balances) {
        balances.every { address, entry ->
                boolean valid = (entry.balance.willetts >= 0) && (entry.balance.willetts <= OmniValue.MAX_WILLETTS) &&
                        (entry.reserved.willetts >= 0) && (entry.reserved.willetts <= OmniValue.MAX_WILLETTS)
                if (!valid) {
                    println("Invalid entry ${entry}")
                }
                return valid;
            }
    }

    def setup() {
        URI baseURL = OmniwalletAbstractClient.omniExplorerApiBase
        boolean debug = true
        client = new OmniwalletClient(baseURL, debug)
    }

    def cleanup() {
    }
}
