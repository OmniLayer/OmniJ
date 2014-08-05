package org.mastercoin.test.rpc

import org.mastercoin.BaseRegTestSpec

import static org.mastercoin.CurrencyID.*

import java.lang.Void as Should

class MSCConsensusSpec extends BaseRegTestSpec {

    Should "Check all balances"() {
        when: "we check Mastercoin balances"
        def balances = getallbalancesforid_MP(MSC)

        then: "we get a list of size >= 0"
        balances != null
        balances.size() >= 0
    }

    Should "Check all balances, raw CLI, type integer"() {
        when: "we check Mastercoin balances"
        def balances = cliSend("getallbalancesforid_MP", MSC as Integer)

        then: "we get a list of size >= 0"
        balances != null
        balances.size() >= 0
    }

    Should "Throw exception checking all balances, raw CLI, type String"() {
        when: "we check Mastercoin balances"
        cliSend("getallbalancesforid_MP", "1")

        then: "Exception is thrown"
        Exception e = thrown()
    }

}
