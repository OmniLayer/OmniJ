package org.mastercoin.test.consensus.omni_www.tetherus

import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static org.mastercoin.CurrencyID.MSC
import static org.mastercoin.CurrencyID.TetherUS

/**
 */
@Title("Compare Master Core vs. Omniwallet www host API for TetherUS currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
                TetherUS)
    }
}
