package foundation.omni.test.consensus.chest.tmsc

import foundation.omni.consensus.ChestConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.TOMNI

/**
 */
@Title("Compare Omni Core vs. Omnichest API for TOMNI currency")
@Ignore("Omni test ecosystem on integration branch now has features not supported on current OmniChest production server")
class CompareCoreChestSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(ChestConsensusTool.ChestHost_Live),
                TOMNI)
    }
}
