package com.msgilligan.bitcoin.rpc

import org.mastercoin.BaseRegTestSpec

import static org.mastercoin.CurrencyID.*
import spock.lang.Shared

/**
 * User: sean
 * Date: 6/28/14
 * Time: 4:59 PM
 */
class MSCConsenusSpec extends BaseRegTestSpec {

    def "Can check all balances"() {
        when: "we check mastercoin balances"
            def balances = getallbalancesforid_MP(MSC)

        then: "we get a list of size <= 1"
            balances.size() <= 1
    }

//    def "Check all balances, raw CLI, type string"() {
//        when: "we check mastercoin balances"
//        def balances = client.cliSend("getallbalancesforid_MP", "1")
//
//        then: "we get a list of size <= 1"
//        balances.size() <= 1
//    }

    def "Check all balances, raw CLI, type integer"() {
        when: "we check mastercoin balances"
        def balances = (List) client.cliSend("getallbalancesforid_MP", MSC.intValue())

        then: "we get a list of size <= 1"
        balances.size() <= 1
    }

}
