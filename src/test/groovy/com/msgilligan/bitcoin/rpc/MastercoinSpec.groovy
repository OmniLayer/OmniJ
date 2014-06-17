package com.msgilligan.bitcoin.rpc

/**
 * User: sean
 * Date: 6/16/14
 * Time: 5:32 PM
 */
class MastercoinSpec extends BaseRPCSpec {
    Long currencyMSC = 1L

    def "mastercoin is not present"() {
        when:
            def destAddr = client.getNewAddress()                   // Create new Bitcoin address
            client.getMPbalance(destAddr, currencyMSC)

        then:
            IOException e = thrown()
            e != null
    }
}
