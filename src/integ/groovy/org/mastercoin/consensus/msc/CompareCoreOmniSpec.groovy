package org.mastercoin.consensus.msc

import static org.mastercoin.CurrencyID.*
import org.mastercoin.consensus.BaseConsensusSpec

/**
 * User: sean
 * Date: 7/9/14
 * Time: 11:56 PM
 */
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(MSC)
    }
}
