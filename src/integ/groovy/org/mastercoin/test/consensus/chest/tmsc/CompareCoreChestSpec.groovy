package org.mastercoin.test.consensus.chest.tmsc

import org.mastercoin.consensus.ChestConsensusTool
import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec

import static org.mastercoin.CurrencyID.TMSC

/**
 */
class CompareCoreChestSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(),
                TMSC)
    }
}
