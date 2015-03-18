package foundation.omni.test.consensus.omni_www.msc

import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MSC

/**
 */
@Title("Compare Master Core vs. Omniwallet www host API for MSC currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
                MSC)
    }
}
