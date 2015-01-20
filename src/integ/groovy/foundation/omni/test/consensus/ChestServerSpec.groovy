package foundation.omni.test.consensus

import foundation.omni.consensus.ChestConsensusTool
import spock.lang.Specification

import static foundation.omni.CurrencyID.MSC


/**
 * Basic functional test for getting consensus data from Chest API
 */
class ChestServerSpec extends Specification {

    def "Can get Chest consensus data"() {
        setup:
        ChestConsensusTool fetcher = new ChestConsensusTool(ChestConsensusTool.ChestHost_Live)

        when: "we get data"
        def snapshot = fetcher.getConsensusSnapshot(MSC)

        then: "something is there"
        snapshot.currencyID == MSC
        snapshot.blockHeight == -1  // Chest doesn't make block height info available (?)
        snapshot.entries.size() >= 1
    }

}