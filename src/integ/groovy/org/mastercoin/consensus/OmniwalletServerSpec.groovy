package org.mastercoin.consensus

import org.mastercoin.CurrencyID
import org.mastercoin.consensus.OmniwalletConsensusTool
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
        def omniSnapshot = omniFetcher.getConsensusSnapshot(CurrencyID.MSC_VALUE)

        then: "it is there"
        omniSnapshot.currencyID == CurrencyID.MSC_VALUE
        omniSnapshot.entries.size() >= 1
    }
}
