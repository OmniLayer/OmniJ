package foundation.omni.test.consensus.omni_www.msc

import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.OMNI

/**
 */
@Title("Compare Omni Core vs. Omniwallet www host API for OMNI currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(),
                OMNI)
    }
}
