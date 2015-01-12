package org.mastercoin.test.consensus.chest.maidsafecoin

import org.mastercoin.consensus.ChestConsensusTool
import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec
import org.mastercoin.test.consensus.chest.BaseChestConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static org.mastercoin.CurrencyID.MaidSafeCoin

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
