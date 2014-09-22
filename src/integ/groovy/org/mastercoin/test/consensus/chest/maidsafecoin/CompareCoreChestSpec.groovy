package org.mastercoin.test.consensus.chest.maidsafecoin

import org.mastercoin.consensus.ChestConsensusTool
import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec
import org.mastercoin.test.consensus.chest.BaseChestConsensusSpec
import spock.lang.Title

import static org.mastercoin.CurrencyID.MaidSafeCoin

/**
 */
@Title("Compare Master Core vs. Masterchest API for MaidSafeCoin currency")
class CompareCoreChestSpec extends BaseChestConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(),
                MaidSafeCoin)
    }
}
