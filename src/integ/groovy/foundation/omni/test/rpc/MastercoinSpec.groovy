package foundation.omni.test.rpc

import foundation.omni.BaseRegTestSpec

import static foundation.omni.CurrencyID.*

/**
 * User: sean
 * Date: 6/16/14
 * Time: 5:32 PM
 */
class MastercoinSpec extends BaseRegTestSpec {

    def "Implement getbalance_MP"() {
        when: "we call getbalance_MP on a newly generated address"
        def destAddr = getNewAddress()                   // Create new Bitcoin address
        def entry = getbalance_MP(destAddr, MSC)

        then: "it should return a balance of zero"
        entry.balance == 0
    }

    def "Return Master Core version field using getinfo_MP" () {
        when: "we request info"
        def mpInfo = getinfo_MP()

        then: "we get back a Mastercoin version, too"
        mpInfo != null
        mpInfo.mastercoreversion != null
        mpInfo.mastercoreversion != ""
    }

}
