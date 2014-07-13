package com.msgilligan.mastercoin.consensus.msc

import com.msgilligan.mastercoin.consensus.BaseConsensusSpec
import spock.lang.Ignore
import spock.lang.Unroll

/**
 * User: sean
 * Date: 7/9/14
 * Time: 11:55 PM
 */
class ListAddressesOnlyInOmni extends BaseConsensusSpec  {

    def "there should be no extra entries" () {
        when:
        def extra = comparison.extraAddressesInSecond()

        then:
        extra.size() == 0
    }

//    @Ignore
//    @Unroll
//    def "#address extra in Omni"() {
//        expect:
//        address == null
//
//        where:
//        address << omniSnapshot.entries.keySet() - mscSnapshot.entries.keySet()
//    }

}
