package foundation.omni.test.consensus.chest.tetherus

import foundation.omni.consensus.ExplorerConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.USDT

/**
 */
@Title("Compare Omni Core vs. Omnichest API for TetherUS currency")
class CompareCoreChestSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ExplorerConsensusTool(ExplorerConsensusTool.ExplorerHost_Live),
                USDT)
    }
}
