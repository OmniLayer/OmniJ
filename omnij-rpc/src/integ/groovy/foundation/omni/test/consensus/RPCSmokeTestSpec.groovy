package foundation.omni.test.consensus

import foundation.omni.BaseMainNetSpec
import foundation.omni.consensus.OmniCoreConsensusTool

import static foundation.omni.CurrencyID.*

class RPCSmokeTestSpec extends BaseMainNetSpec {

    def "Omni Core RPC is working" () {
        setup: "no setup required here"

        expect: "client is not null"
        client != null

        when: "we request info"
        def info = getInfo()

        then: "we get back some version information, too"
        info != null
        info.version >= 90000
    }

    def "Can get Omni Core consensus data"() {
        setup:
        def mscFetcher = new OmniCoreConsensusTool(client)

        when: "we get data"
        def mscSnapshot = mscFetcher.getConsensusSnapshot(MSC)

        then: "it is there"
        mscSnapshot.currencyID == MSC
        mscSnapshot.entries.size() >= 1
    }

}
