package org.mastercoin.consensus.tmsc

import spock.lang.Ignore

import static org.mastercoin.CurrencyID.*
import org.mastercoin.consensus.BaseConsensusSpec

/**
 * User: sean
 * Date: 7/10/14
 * Time: 1:36 AM
 */
@Ignore
class CompareCoreOmniSpec extends BaseConsensusSpec {

    void setupSpec() {
        setupComparisonForCurrency(TMSC)
    }
}
