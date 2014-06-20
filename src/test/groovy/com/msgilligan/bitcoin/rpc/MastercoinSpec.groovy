package com.msgilligan.bitcoin.rpc

import org.junit.Ignore

/**
 * User: sean
 * Date: 6/16/14
 * Time: 5:32 PM
 */
class MastercoinSpec extends BaseRPCSpec {
    Long currencyMSC = 1L

    def "mastercoin is present"() {
        when: "we call getMPbalance on a newly generated address"
            def destAddr = client.getNewAddress()                   // Create new Bitcoin address
            def balance = client.getMPbalance(destAddr, currencyMSC)

        then: "it should return a balance of zero"
            balance == 0
    }

    def "returns mastercoin version along with basic info" () {
        when: "we request info"
        def info = client.getInfo()

        then: "we get back some mastercoin information, too"
        info != null
        info.mastercoreversion == 10100
    }

}
