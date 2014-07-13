package com.msgilligan.mastercoin.consensus.msc

import com.msgilligan.mastercoin.consensus.BaseConsensusSpec
import spock.lang.Unroll

/**
 * User: sean
 * Date: 7/10/14
 * Time: 1:26 AM
 */
class CompareCoreOmniReservedSpec extends BaseConsensusSpec {

    @Unroll
    def "compare #address reserved msc vs omni (#mscReserved == #omniReserved)"() {
        expect:
        omniReserved == mscReserved

        where:
        address << comparison.addressIntersection()
        omniReserved = omniSnapshot.entries[address].reserved
        mscReserved = mscSnapshot.entries[address].reserved
    }

}
