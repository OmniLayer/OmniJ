package com.msgilligan.bitcoin.rpc

import org.junit.Ignore

/**
 * User: sean
 * Date: 6/16/14
 * Time: 5:32 PM
 */
class MastercoinSpec extends BaseRPCSpec {
    Long currencyMSC = 1L

    def "mastercoin is not present"() {
        when: "mastercoin is not present and we call a mastercoin method"
            def destAddr = client.getNewAddress()                   // Create new Bitcoin address
            client.getMPbalance(destAddr, currencyMSC)

        then: "it should throw an exception with status code 404"
            IOException e = thrown()
            e != null
    }

    def "returns mastercoin version along with basic info" () {
        when: "we request info"
        def info = client.getInfo()

        then: "we get back some mastercoin information, too"
        info != null
//        info.mastercoreversion == 10100
    }

}
