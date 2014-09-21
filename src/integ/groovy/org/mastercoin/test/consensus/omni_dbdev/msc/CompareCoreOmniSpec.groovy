package org.mastercoin.test.consensus.omni_dbdev.msc

import org.mastercoin.consensus.OmniwalletConsensusTool

import static org.mastercoin.CurrencyID.*
import org.mastercoin.test.consensus.BaseConsensusSpec

/**
 */
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_DBDev),
                MSC)
    }
}
