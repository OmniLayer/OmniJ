package foundation.omni.test.rpc.basic

import foundation.omni.BaseRegTestSpec

import static foundation.omni.CurrencyID.*

/**
 * Two quick checks that will work on Omni Core but not Bitcoin Core.
 * Maybe they should be moved or removed for being redundant.
 */
class OmniSpec extends BaseRegTestSpec {

    def "Implement getbalance_MP"() {
        when: "we call getbalance_MP on a newly generated address"
        def destAddr = getNewAddress()                   // Create new Bitcoin address
        def entry = getbalance_MP(destAddr, MSC)

        then: "it should return a balance of zero"
        entry.balance == 0
    }

    def "Return Omni Core version field using getinfo_MP" () {
        when: "we request info"
        def mpInfo = getinfo_MP()

        then: "we get back an Omni version, too"
        mpInfo != null
        mpInfo.mastercoreversion != null
        mpInfo.mastercoreversion != ""
    }

}
