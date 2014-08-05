package org.mastercoin.test.consensus

import org.mastercoin.consensus.OmniwalletConsensusTool

import static org.mastercoin.CurrencyID.*
import spock.lang.Specification

/**
 * User: sean
 * Date: 7/20/14
 * Time: 1:08 AM
 */
class OmniwalletServerSpec extends Specification {
    def "Can get Omniwallet consensus data"() {
        setup:
        OmniwalletConsensusTool omniFetcher = new OmniwalletConsensusTool()

        when: "we get data"
        def omniSnapshot = omniFetcher.getConsensusSnapshot(MSC)

        then: "it is there"
        omniSnapshot.currencyID == MSC
        omniSnapshot.entries.size() >= 1
    }
}
