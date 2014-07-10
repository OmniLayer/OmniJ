package com.msgilligan.mastercoin.consensus

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * User: sean
 * Date: 7/9/14
 * Time: 4:40 PM
 */
class DataPipeSpec extends Specification  {

    @Shared
    Map<String, Integer> omniBalances
    @Shared
    Map<String, Integer> mscBalances

    void setupSpec() {
        omniBalances = [a: new ConsensusEntry(address:"a", balance: 0), b: new ConsensusEntry(address:"b", balance: 1)]
        mscBalances = [a: new ConsensusEntry(address:"a", balance: 0), b: new ConsensusEntry(address:"b", balance: 1)]
    }

    @Unroll
    def "#address balances compare (#omniBalance == #mscBalance)"() {
        expect:
        omniBalance == mscBalance

        where:
        address << omniBalances.keySet()
        omniBalance = omniBalances[address].balance
        mscBalance = mscBalances[address].balance
    }

}
