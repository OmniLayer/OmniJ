package foundation.omni.test.consensus.omni_www.tetherus

import foundation.omni.consensus.OmniwalletConsensusTool
import foundation.omni.test.consensus.BaseConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static foundation.omni.CurrencyID.USDT

/**
 */
@Title("Compare Omni Core vs. Omniwallet www host API for TetherUS currency")
@Ignore("Disabled because Omniwallet times out and returns a 504")
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new OmniwalletConsensusTool(),
                USDT)
    }
}
