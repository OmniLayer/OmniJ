package org.mastercoin.test.consensus.chest.tmsc

import org.mastercoin.consensus.ChestConsensusTool
import org.mastercoin.consensus.OmniwalletConsensusTool
import org.mastercoin.test.consensus.BaseConsensusSpec
import org.mastercoin.test.consensus.chest.BaseChestConsensusSpec
import spock.lang.Ignore
import spock.lang.Title

import static org.mastercoin.CurrencyID.TMSC

/**
 */
@Ignore
@Title("Compare Master Core vs. Masterchest API for TMSC currency")
class CompareCoreChestSpec extends BaseChestConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(new ChestConsensusTool(),
                TMSC)
    }
}
