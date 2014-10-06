package org.mastercoin.test.consensus.chest

import org.mastercoin.BaseMainNetSpec
import org.mastercoin.CurrencyID
import org.mastercoin.consensus.ConsensusComparison
import org.mastercoin.consensus.ConsensusFetcher
import org.mastercoin.consensus.MasterCoreConsensusTool
import spock.lang.Shared
import spock.lang.Subject
import spock.lang.Unroll


/**
 * Special case base class for Chest comparison tests
 * No blockHeight comparison for now
 * Only compare balance, since current Chest API doesn't return reserved.
 */
abstract class BaseChestConsensusSpec extends BaseMainNetSpec {
    @Shared @Subject
    ConsensusComparison comparison

    /**
     * Set up a ConsensusComparison for a particular currency
     * @param referenceFetcher ConsensusFetcher for a (remote) reference server
     * @param id CurrencyID to compare
     */
    void setupComparisonForCurrency(ConsensusFetcher referenceFetcher, CurrencyID id) {
        def mscFetcher = new MasterCoreConsensusTool(client)
        def mscSnapshot = mscFetcher.getConsensusSnapshot(id)

        def referenceSnapshot = referenceFetcher.getConsensusSnapshot(id)

        comparison = new ConsensusComparison(mscSnapshot, referenceSnapshot)
    }

    @Unroll
    def "#address #entry1 == #entry2"() {
        expect:
        entry1.balance == entry2.balance

        where:
        [address, entry1, entry2] << comparison
    }

}