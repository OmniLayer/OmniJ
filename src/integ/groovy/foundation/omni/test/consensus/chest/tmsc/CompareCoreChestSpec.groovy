package foundation.omni.test.consensus.chest.tmsc

import foundation.omni.consensus.ChestConsensusTool
import foundation.omni.test.consensus.chest.BaseChestConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.TMSC

/**
 */
@Ignore
@Title("Compare Master Core vs. Omnichest API for TMSC currency")
class CompareCoreChestSpec extends BaseChestConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(ChestConsensusTool.ChestHost_Live),
                TMSC)
    }
}
