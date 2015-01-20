package foundation.omni.test.consensus.chest.maidsafecoin

import foundation.omni.consensus.ChestConsensusTool
import foundation.omni.test.consensus.chest.BaseChestConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.MaidSafeCoin

/**
 */
@Ignore
@Title("Compare Master Core vs. Omnichest API for MaidSafeCoin currency")
class CompareCoreChestSpec extends BaseChestConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(ChestConsensusTool.ChestHost_Live),
                MaidSafeCoin)
    }
}
