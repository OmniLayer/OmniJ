package org.mastercoin.test.consensus.chest_core.msc

import org.mastercoin.consensus.ChestConsensusTool
import org.mastercoin.test.consensus.chest.BaseChestConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static org.mastercoin.CurrencyID.MSC


/**
 *
 */
@Ignore
@Title("Compare Master Core vs. Masterchest API (with Master Core backend) for MSC currency")
class CompareCoreChestCoreSpec extends BaseChestConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(ChestConsensusTool.ChestHost_Core),
                MSC)
    }
}
