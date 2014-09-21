package org.mastercoin.test.consensus

import org.mastercoin.BaseMainNetSpec
import org.mastercoin.CurrencyID
import org.mastercoin.consensus.ConsensusComparison
import org.mastercoin.consensus.ConsensusFetcher
import org.mastercoin.consensus.MasterCoreConsensusTool
import org.mastercoin.consensus.OmniwalletConsensusTool
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Base class for Consensus Comparisons
 * Makes sure the block height of both snapshots matches and
 * also compares the balance of every address in the <b>union</b> of the two snapshots.
 */
abstract class  BaseConsensusSpec extends BaseMainNetSpec {
    @Shared
    ConsensusComparison comparison

    /**
     * Set up a ConsensusComparison for a particular currency
     * @param referenceFetcher ConsensusFetcher for a (remote) reference server
     * @param id CurrencyID to compare
     */
    void setupComparisonForCurrency(ConsensusFetcher referenceFetcher, CurrencyID id) {
        def mscFetcher = new MasterCoreConsensusTool(client)
        def mscSnapshot = mscFetcher.getConsensusSnapshot(id)

        def omniSnapshot = referenceFetcher.getConsensusSnapshot(id)

        comparison = new ConsensusComparison(mscSnapshot, omniSnapshot)
    }

    def "block height is the same in both snapshots"() {
        given:
        def blockHeight1 = comparison.c1.blockHeight
        def blockHeight2 = comparison.c2.blockHeight

        expect:
        blockHeight1 == blockHeight2
    }

    @Unroll
    def "#address #entry1 == #entry2"() {
        expect:
        entry1 == entry2

        where:
        [address, entry1, entry2] << comparison
    }

}
