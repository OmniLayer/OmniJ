package org.mastercoin.test.consensus.omni_dbdev.msc

import org.mastercoin.consensus.OmniwalletConsensusTool
import spock.lang.Title

import static org.mastercoin.CurrencyID.*
import org.mastercoin.test.consensus.BaseConsensusSpec

/**
 */
@Title("Compare Master Core vs. Omniwallet dbdev host API for MSC currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_DBDev),
                MSC)
    }
}
