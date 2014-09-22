package org.mastercoin.test.consensus.omni_www.maidsafecoin

import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec
import spock.lang.Title

import static org.mastercoin.CurrencyID.MaidSafeCoin

/**
 */
@Title("Compare Master Core vs. Omniwallet www host API for MaidSafeCoin currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
        MaidSafeCoin)
    }
}
