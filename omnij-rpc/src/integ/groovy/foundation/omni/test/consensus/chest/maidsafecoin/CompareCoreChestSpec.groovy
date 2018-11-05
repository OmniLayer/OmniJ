package foundation.omni.test.consensus.chest.maidsafecoin

import foundation.omni.consensus.ExplorerConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MAID

/**
 */
@Title("Compare Omni Core vs. Omnichest API for MaidSafeCoin currency")
class CompareCoreChestSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ExplorerConsensusTool(ExplorerConsensusTool.ExplorerHost_Live),
                MAID)
    }
}
