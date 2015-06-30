package foundation.omni.test.consensus.omni_www.maidsafecoin

import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static foundation.omni.CurrencyID.MaidSafeCoin

/**
 */
@Title("Compare Omni Core vs. Omniwallet www host API for MaidSafeCoin currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
        MaidSafeCoin)
    }
}
