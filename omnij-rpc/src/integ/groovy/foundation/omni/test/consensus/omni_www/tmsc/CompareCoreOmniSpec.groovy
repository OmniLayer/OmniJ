package foundation.omni.test.consensus.omni_www.tmsc

import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.TMSC

/**
 */
@Title("Compare Omni Core vs. Omniwallet www host API for TMSC currency")
@Ignore("Omni test ecosystem on integration branch now has features not supported on current Omniwallet production server")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(),
                TMSC)
    }
}
