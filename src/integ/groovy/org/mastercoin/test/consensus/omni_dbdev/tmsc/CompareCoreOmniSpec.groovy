package org.mastercoin.test.consensus.omni_dbdev.tmsc

import org.mastercoin.consensus.OmniwalletConsensusTool
import spock.lang.Ignore

import static org.mastercoin.CurrencyID.*
import org.mastercoin.test.consensus.BaseConsensusSpec

/**
 */
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(OmniwalletConsensusTool.OmniHost_DBDev),
                TMSC)
    }
}
