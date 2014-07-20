package org.mastercoin.consensus.msc

import org.mastercoin.consensus.BaseConsensusSpec
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
        mscReserved == omniReserved

        where:
        [address, entry1, entry2] << comparison.intersection()
        mscReserved = entry1.reserved
        omniReserved = entry2.reserved
    }

}
