package org.mastercoin.consensus

import com.msgilligan.bitcoin.rpc.MastercoinClient
import groovy.json.JsonSlurper
import org.mastercoin.BaseMainNetSpec
import org.mastercoin.CurrencyID
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * User: sean
 * Date: 7/9/14
 * Time: 11:31 PM
 */
abstract class  BaseConsensusSpec extends BaseMainNetSpec {
    @Shared
    ConsensusComparison comparison

    void setupComparisonForCurrency(CurrencyID id) {
        def mscFetcher = new MasterCoreConsensusTool()
        def mscSnapshot = mscFetcher.getConsensusSnapshot(id.value)

        def omniFetcher = new OmniwalletConsensusTool()
        def omniSnapshot = omniFetcher.getConsensusSnapshot(id.value)

        comparison = new ConsensusComparison(mscSnapshot, omniSnapshot)
    }

    @Unroll
    def "#address #entry1 == #entry2"() {
        expect:
        entry1 == entry2

        where:
        [address, entry1, entry2] << comparison
    }

}
