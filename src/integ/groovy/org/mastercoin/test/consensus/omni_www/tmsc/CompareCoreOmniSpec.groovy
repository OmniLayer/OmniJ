package org.mastercoin.test.consensus.omni_www.tmsc

import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec
import spock.lang.Ignore

import static org.mastercoin.CurrencyID.TMSC

/**
 */
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_Live),
                TMSC)
    }
}
