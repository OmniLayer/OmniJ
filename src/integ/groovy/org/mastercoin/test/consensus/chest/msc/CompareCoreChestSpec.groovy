package org.mastercoin.test.consensus.chest.msc

import org.mastercoin.consensus.ChestConsensusTool
import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec
import org.mastercoin.test.consensus.chest.BaseChestConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static org.mastercoin.CurrencyID.MSC

/**
 */
@Ignore
@Title("Compare Master Core vs. Masterchest API for MSC currency")
class CompareCoreChestSpec extends BaseChestConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(ChestConsensusTool.ChestHost_Live),
                MSC)
    }
}
