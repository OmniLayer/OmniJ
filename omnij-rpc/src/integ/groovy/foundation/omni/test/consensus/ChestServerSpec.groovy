package foundation.omni.test.consensus

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.consensus.ChestConsensusTool
import foundation.omni.rpc.SmartPropertyListInfo
import spock.lang.Ignore
import spock.lang.Specification

import static foundation.omni.CurrencyID.OMNI
import static foundation.omni.CurrencyID.TOMNI
import static foundation.omni.CurrencyID.MAID
import static foundation.omni.CurrencyID.USDT

/**
 * Basic functional test for getting consensus data from Chest API
 */
class ChestServerSpec extends Specification {

    def "Can get block height"() {
        setup:
        ChestConsensusTool fetcher = new ChestConsensusTool(ChestConsensusTool.ChestHost_Live)

        when: "we get block height"
        def blockHeight = fetcher.currentBlockHeight()

        then: "it looks reasonable"
        blockHeight > 323000  // Greater than a relatively recent main-net block
    }

    @Ignore("Needs update for new API")
    def "Can get Chest consensus data (divisible)"() {
        setup:
        ChestConsensusTool fetcher = new ChestConsensusTool(ChestConsensusTool.ChestHost_Live)

        when: "we get data"
        def snapshot = fetcher.getConsensusSnapshot(OMNI)

        then: "something is there"
        snapshot.currencyID == OMNI
        snapshot.blockHeight > 323000  // Greater than a relatively recent main-net block
        snapshot.entries.size() >= 1
    }

    @Ignore("Needs update for new API")
    def "Can get Chest consensus data (indivisible)"() {
        setup:
        ChestConsensusTool fetcher = new ChestConsensusTool(ChestConsensusTool.ChestHost_Live)

        when: "we get data"
        def snapshot = fetcher.getConsensusSnapshot(MAID)

        then: "something is there"
        snapshot.currencyID == MAID
        snapshot.blockHeight > 323000  // Greater than a relatively recent main-net block
        snapshot.entries.size() >= 1
    }

    def "Can get Chest property list"() {
        setup:
        ChestConsensusTool fetcher = new ChestConsensusTool(ChestConsensusTool.ChestHost_Live)

        when: "we get data"
        def properties = fetcher.listProperties()

        then: "we get a list of size >= 2"
        properties != null
        properties.size() >= 2

        when: "we convert the list to a map"
        // This may be unnecessary if we can assume the property list is ordered by propertyid
        Map<CurrencyID, SmartPropertyListInfo> props = properties.collect { [it.propertyid, it] }.collectEntries()
        
        then: "OMNI and TOMNI are NOW returned in Chest property list"
        props[OMNI] != null
        props[TOMNI] != null

        and: "MAID is as expected"
        props[MAID].propertyid == MAID
        props[MAID].propertyid.ecosystem == Ecosystem.OMNI
        props[MAID].name == "MaidSafeCoin"
        props[MAID].category == "Crowdsale"
        props[MAID].subcategory == "MaidSafe"
        props[MAID].data == "SAFE Network Crowdsale (MSAFE)"
        props[MAID].url == "www.buysafecoins.com"
        props[MAID].divisible == false

        and: "USDT is as expected"
        props[USDT].propertyid == USDT
        props[USDT].propertyid.ecosystem == Ecosystem.OMNI
        props[USDT].name == "TetherUS"
        props[USDT].category == "Financial and insurance activities"
        props[USDT].subcategory == "Activities auxiliary to financial service and insurance activities"
        props[USDT].data == "The next paradigm of money."
        props[USDT].url == "https://tether.to"
        props[USDT].divisible == true

    }
}