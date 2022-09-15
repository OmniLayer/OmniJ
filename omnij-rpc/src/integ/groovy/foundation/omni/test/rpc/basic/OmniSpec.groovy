package foundation.omni.test.rpc.basic

import foundation.omni.BaseRegTestSpec

import static foundation.omni.CurrencyID.*

/**
 * Two quick checks that will work on Omni Core but not Bitcoin Core.
 * Maybe they should be moved or removed for being redundant.
 */
class OmniSpec extends BaseRegTestSpec {

    def "Reports that server is an Omni server"() {
        when:
        def isOmni = isOmniServer().join()

        then:
        isOmni
    }

    def "Implements omni_getbalance"() {
        when: "we call omni_getbalance on a newly generated address"
        def destAddr = getNewAddress()                   // Create new Bitcoin address
        def entry = omniGetBalance(destAddr, OMNI)

        then: "it should return a balance of zero"
        entry.balance == 0
    }

    def "Returns Omni Core version field using omni_getinfo" () {
        when: "we request info"
        def mpInfo = omniGetInfo()

        then: "we get back an Omni version, too"
        mpInfo != null
        mpInfo.mastercoreversion != null
        mpInfo.mastercoreversion != ""
    }

}
