package foundation.omni.test.consensus.omni_www.tetherus

import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.TetherUS

/**
 */
@Title("Compare Omni Core vs. Omniwallet www host API for TetherUS currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(),
                TetherUS)
    }
}
