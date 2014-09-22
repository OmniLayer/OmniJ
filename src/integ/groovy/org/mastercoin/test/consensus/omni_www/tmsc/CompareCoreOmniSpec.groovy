package org.mastercoin.test.consensus.omni_www.tmsc

import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static org.mastercoin.CurrencyID.TMSC

/**
 */
@Ignore
@Title("Compare Master Core vs. Omniwallet www host API for TMSC currency")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
                TMSC)
    }
}
