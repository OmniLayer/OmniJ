package org.mastercoin.test.consensus.omni_www.msc

import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec

import static org.mastercoin.CurrencyID.MSC

/**
 */
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
                MSC)
    }
}
