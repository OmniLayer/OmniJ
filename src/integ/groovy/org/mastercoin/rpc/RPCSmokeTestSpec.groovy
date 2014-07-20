package org.mastercoin.rpc

import org.mastercoin.BaseMainNetSpec
import org.mastercoin.CurrencyID
import org.mastercoin.consensus.MasterCoreConsensusTool

/**
 * User: sean
 * Date: 7/20/14
 * Time: 1:02 AM
 */
class RPCSmokeTestSpec extends BaseMainNetSpec {
    def "Master Core RPC is working" () {
        when: "we request info"
        def info = client.getInfo()

        then: "we get back some mastercoin information, too"
        info != null
        info.mastercoreversion >= 10003
    }

    def "Can get Mastercore consensus data"() {
        setup:
        MasterCoreConsensusTool mscFetcher = new MasterCoreConsensusTool()

        when: "we get data"
        def mscSnapshot = mscFetcher.getConsensusSnapshot(CurrencyID.MSC_VALUE)

        then: "it is there"
        mscSnapshot.currencyID ==  CurrencyID.MSC_VALUE
        mscSnapshot.entries.size() >= 1
    }

}
