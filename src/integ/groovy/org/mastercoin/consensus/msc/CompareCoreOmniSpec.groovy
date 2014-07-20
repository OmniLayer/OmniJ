package org.mastercoin.consensus.msc

import org.mastercoin.consensus.BaseConsensusSpec
import spock.lang.Unroll

/**
 * User: sean
 * Date: 7/9/14
 * Time: 11:56 PM
 */
class CompareCoreOmniSpec extends BaseConsensusSpec {

    @Unroll
    def "compare #address balance msc vs omni (#mscBalance == #omniBalance)"() {
        expect:
        mscBalance == omniBalance

        where:
        [address, entry1, entry2] << comparison.intersection()
        mscBalance = entry1.balance
        omniBalance = entry2.balance
    }

}
