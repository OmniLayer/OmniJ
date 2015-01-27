package foundation.omni.test.consensus.chest.tetherus

import foundation.omni.consensus.ChestConsensusTool
import foundation.omni.test.consensus.chest.BaseChestConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MSC
import static foundation.omni.CurrencyID.TetherUS

/**
 */
@Title("Compare Master Core vs. Omnichest API for TetherUS currency")
class CompareCoreChestSpec extends BaseChestConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(ChestConsensusTool.ChestHost_Live),
                TetherUS)
    }
}
