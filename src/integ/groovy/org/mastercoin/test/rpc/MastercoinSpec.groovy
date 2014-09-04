package org.mastercoin.test.rpc

import org.mastercoin.BaseRegTestSpec

import static org.mastercoin.CurrencyID.*
import java.lang.Void as Should

/**
 * User: sean
 * Date: 6/16/14
 * Time: 5:32 PM
 */
class MastercoinSpec extends BaseRegTestSpec {

    Should "Implement getbalance_MP"() {
        when: "we call getbalance_MP on a newly generated address"
        def destAddr = getNewAddress()                   // Create new Bitcoin address
        def entry = getbalance_MP(destAddr, MSC)

        then: "it should return a balance of zero"
        entry.balance == 0
    }

    Should "Return Mastercoin version field along with Bitcoin info fields" () {
        when: "we request info"
        def info = getInfo()

        then: "we get back a Mastercoin version, too"
        info != null
        info.mastercoreversion >= 10003
    }

}
