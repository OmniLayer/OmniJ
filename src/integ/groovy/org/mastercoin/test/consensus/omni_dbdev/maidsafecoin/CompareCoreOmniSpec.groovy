package org.mastercoin.test.consensus.omni_dbdev.maidsafecoin

import org.mastercoin.consensus.OmniwalletConsensusTool
import spock.lang.Title

import static org.mastercoin.CurrencyID.*
import org.mastercoin.test.consensus.BaseConsensusSpec

/**
 */
@Title("Compare Master Core vs. Omniwallet dbdev host API for MaidSafeCoin currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_DBDev),
                MaidSafeCoin)
    }
}
