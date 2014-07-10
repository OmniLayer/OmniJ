package com.msgilligan.mastercoin.consensus.msc

import com.msgilligan.mastercoin.consensus.BaseConsensusSpec
import spock.lang.Unroll

/**
 * User: sean
 * Date: 7/9/14
 * Time: 11:55 PM
 */
class VsOmniExtra extends BaseConsensusSpec  {
    @Unroll
    def "#address extra in Master"() {
        expect:
        address == null

        where:
        address << mscSnapshot.entries - omniSnapshot.entries
    }
}
