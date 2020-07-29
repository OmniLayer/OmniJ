package foundation.omni.test.consensus

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.netapi.omniwallet.OmniwalletAbstractClient
import foundation.omni.rest.omniwallet.mjdk.OmniwalletModernJDKClient
import foundation.omni.rpc.SmartPropertyListInfo
import org.bitcoinj.params.MainNetParams
import spock.lang.Unroll

import static foundation.omni.CurrencyID.*
import spock.lang.Specification

/**
 * Functional test for getting consensus data from Omni API
 */
class OmniwalletServerSpec extends Specification {
    
    def "Groovy handles Omniwallet TLS (CloudFlare) correctly"() {
        setup:
        def owExtraHeaders = ['User-Agent': 'OmniJ Test Script']
        def url = new URL("https://www.omniwallet.org:/v1/system/revision.json")

        when:
        def text =  url.getText([requestProperties: owExtraHeaders])

        then:
        text.startsWith('{')
    }

    def "Can get Omniwallet block height"() {
        setup:
        OmniwalletAbstractClient omniFetcher = getOmniwalletClient()

        when: "we get a block height"
        /* Private method, but we can still call it with Groovy for a test */
        def blockHeight = omniFetcher.currentBlockHeight()

        then: "it looks valid"
        /* TODO:  Check to make sure it's within 15 blocks of Omni Core, or something like that? */
        blockHeight > 323000
    }

    @Unroll
    def "Can get Omniwallet consensus data (divisible, #propId)"(CurrencyID propId) {
        setup:
        OmniwalletAbstractClient omniFetcher = getOmniwalletClient()

        when: "we get data"
        def omniSnapshot = omniFetcher.getConsensusSnapshot(propId)

        then: "something is there"
        omniSnapshot.currencyID == propId
        omniSnapshot.blockHeight > 323000  // Greater than a relatively recent main-net block
        omniSnapshot.entries.size() >= 1

        where:
        propId << [OMNI, TOMNI, /* USDT */]
    }

    @Unroll
    def "Can get Omniwallet consensus data (indivisible, #propId)"(CurrencyID propId) {
        setup:
        OmniwalletAbstractClient omniFetcher = getOmniwalletClient()

        when: "we get data"
        def omniSnapshot = omniFetcher.getConsensusSnapshot(propId)

        then: "something is there"
        omniSnapshot.currencyID == propId
        omniSnapshot.blockHeight > 323000  // Greater than a relatively recent main-net block
        omniSnapshot.entries.size() >= 1

        where:
        propId << [MAID, SAFEX]
    }

    def "Can get Omniwallet property list"() {
        setup:
        OmniwalletAbstractClient omniFetcher = getOmniwalletClient()

        when: "we get data"
        def properties = omniFetcher.listProperties()

        then: "we get a list of size >= 2"
        properties != null
        properties.size() >= 2

        when: "we convert the list to a map"
        // This may be unnecessary if we can assume the property list is ordered by propertyid
        Map<CurrencyID, SmartPropertyListInfo> props = properties.collect{[it.propertyid, it]}.collectEntries()

        then: "OMNI is as expected"
        props[OMNI].propertyid == OMNI
        props[OMNI].propertyid.ecosystem == Ecosystem.OMNI
        props[OMNI].name == "Omni Token"
        props[OMNI].category == "N/A"
        props[OMNI].subcategory == "N/A"
        props[OMNI].data == "Omni tokens serve as the binding between Bitcoin, smart properties and contracts created on the Omni Layer."
        props[OMNI].url == "http://www.omnilayer.org"
        props[OMNI].divisible == true

        and: "MAID is as expected"
        props[MAID].propertyid == MAID
        props[MAID].propertyid.ecosystem == Ecosystem.OMNI
        props[MAID].name == "MaidSafeCoin"
        props[MAID].category == "Crowdsale"
        props[MAID].subcategory == "MaidSafe"
        props[MAID].data == "SAFE Network Crowdsale (MSAFE)"
        props[MAID].url == "www.buysafecoins.com"
        props[MAID].divisible == false

        and: "TOMNI is as expected"
        props[TOMNI].propertyid == TOMNI
        props[TOMNI].propertyid.ecosystem == Ecosystem.TOMNI
        props[TOMNI].name == "Test Omni Token"
        props[TOMNI].category == "N/A"
        props[TOMNI].subcategory == "N/A"
        props[TOMNI].data == "Test Omni tokens serve as the binding between Bitcoin, smart properties and contracts created on the Omni Layer."
        props[TOMNI].url == "http://www.omnilayer.org"
        props[TOMNI].divisible == true

// TODO: Re-enable once timeout issues with USDT are fixed.
//        and: "USDT is as expected"
//        props[USDT].propertyid == USDT
//        props[USDT].propertyid.ecosystem == Ecosystem.OMNI
//        props[USDT].name == "TetherUS"
//        props[USDT].category == "Financial and insurance activities"
//        props[USDT].subcategory == "Activities auxiliary to financial service and insurance activities"
//        props[USDT].data == "The next paradigm of money."
//        props[USDT].url == "https://tether.to"
//        props[USDT].divisible == true
    }

    private static OmniwalletAbstractClient getOmniwalletClient() {
        return new OmniwalletModernJDKClient(OmniwalletAbstractClient.omniwalletBase, false, true, MainNetParams.get())
    }
}
