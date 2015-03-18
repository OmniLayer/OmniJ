package foundation.omni.test.consensus

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.rpc.SmartPropertyListInfo

import static foundation.omni.CurrencyID.*
import spock.lang.Specification

/**
 * Functional test for getting consensus data from Omni API
 */
class OmniwalletServerSpec extends Specification {

    def "Can get Omniwallet block height"() {
        setup:
        OmniwalletConsensusTool omniFetcher = new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live)

        when: "we get a block height"
        /* Private method, but we can still call it with Groovy for a test */
        def blockHeight = omniFetcher.currentBlockHeight()

        then: "it looks valid"
        /* TODO:  Check to make sure it's within 15 blocks of Master Core, or something like that? */
        blockHeight >= 315121
    }

    def "Can get Omniwallet consensus data"() {
        setup:
        OmniwalletConsensusTool omniFetcher = new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live)

        when: "we get data"
        def omniSnapshot = omniFetcher.getConsensusSnapshot(MSC)

        then: "something is there"
        omniSnapshot.currencyID == MSC
        omniSnapshot.blockHeight > 323000  // Greater than a relatively recent main-net block
        omniSnapshot.entries.size() >= 1
    }

    def "Can get Omniwallet property list"() {
        setup:
        OmniwalletConsensusTool omniFetcher = new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live)

        when: "we get data"
        def properties = omniFetcher.listProperties()

        then: "we get a list of size >= 2"
        properties != null
        properties.size() >= 2

        when: "we convert the list to a map"
        // This may be unnecessary if we can assume the property list is ordered by propertyid
        Map<CurrencyID, SmartPropertyListInfo> props = properties.collect{[it.id, it]}.collectEntries()

        then: "we can check MSC and TMSC are as expected"
        props[MSC].id == MSC
        props[MSC].id.ecosystem == Ecosystem.MSC
        props[MSC].name == "Mastercoin" // Note: Omni Core returns "MasterCoin" with a capital-C
        props[MSC].category == ""
        props[MSC].subCategory == ""
        props[MSC].data == ""
        props[MSC].url == ""
        props[MSC].divisible == null

        props[TMSC].id == TMSC
        props[TMSC].id.ecosystem == Ecosystem.TMSC
        props[TMSC].name == "Test Mastercoin" // Note: Omni Core returns "Mastercoin" with a capital-C
        props[TMSC].category == ""
        props[TMSC].subCategory == ""
        props[TMSC].data == ""
        props[TMSC].url == ""
        props[TMSC].divisible == null
    }
}
