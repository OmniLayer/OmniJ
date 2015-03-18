package foundation.omni.test.consensus.omni_www.tmsc

import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.TMSC

/**
 */
@Title("Compare Master Core vs. Omniwallet www host API for TMSC currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
                TMSC)
    }
}
