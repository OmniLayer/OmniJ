package org.mastercoin.test.consensus.omni_www.maidsafecoin

import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec

import static org.mastercoin.CurrencyID.MaidSafeCoin

/**
 */
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
        MaidSafeCoin)
    }
}
