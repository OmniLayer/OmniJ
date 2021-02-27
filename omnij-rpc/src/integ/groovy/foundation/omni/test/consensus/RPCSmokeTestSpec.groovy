package foundation.omni.test.consensus

import foundation.omni.BaseMainNetSpec
import foundation.omni.consensus.OmniCoreConsensusTool
import foundation.omni.net.OmniMainNetParams
import foundation.omni.json.pojo.OmniPropertyInfo

import static foundation.omni.CurrencyID.*

class RPCSmokeTestSpec extends BaseMainNetSpec {

    def "Omni Core RPC is working" () {
        setup: "no setup required here"

        expect: "client is not null"
        client != null

        when: "we request info"
        def info = getNetworkInfo()

        then: "we get back some version information, too"
        info != null
        info.version >= 100400
    }

    def "Can get Omni Core consensus data"() {
        setup:
        def mscFetcher = new OmniCoreConsensusTool(client)

        when: "we get data"
        def mscSnapshot = mscFetcher.getConsensusSnapshot(OMNI)

        then: "it is there"
        mscSnapshot.currencyID == OMNI
        mscSnapshot.entries.size() >= 1
    }

    def "omniGetAllBalancesForAddress(exodusAddress) is working"() {
        when:
        def balances = omniGetAllBalancesForAddress(OmniMainNetParams.get().exodusAddress)

        then:
        balances[OMNI].balance >= 0.divisible
        balances[OMNI].reserved >= 0.divisible
    }

    def "omniGetProperty(OMNI) is working"() {
        when:
        OmniPropertyInfo info = omniGetProperty(OMNI)

        then:
        info.totaltokens > 1000
    }

}
