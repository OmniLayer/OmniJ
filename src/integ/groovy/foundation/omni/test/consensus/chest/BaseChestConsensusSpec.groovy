package foundation.omni.test.consensus.chest

import foundation.omni.BaseMainNetSpec
import foundation.omni.CurrencyID
import foundation.omni.consensus.ConsensusComparison
import foundation.omni.consensus.ConsensusFetcher
import foundation.omni.consensus.OmniCoreConsensusTool
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
        def mscFetcher = new OmniCoreConsensusTool(client)
        def mscSnapshot = mscFetcher.getConsensusSnapshot(id)

        def referenceSnapshot = referenceFetcher.getConsensusSnapshot(id)

        comparison = new ConsensusComparison(mscSnapshot, referenceSnapshot)
    }

    @Unroll
    def "blockHeight #leftHeight == #rightHeight (#currency)"() {
        expect:
        leftHeight == rightHeight

        where:
        currency = comparison.c1.currencyID
        leftHeight = comparison.c1.blockHeight
        rightHeight = comparison.c2.blockHeight
    }

    @Unroll
    def "#address #entry1 == #entry2"() {
        expect:
        entry1.balance == entry2.balance

        where:
        [address, entry1, entry2] << comparison
    }

}