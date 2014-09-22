package org.mastercoin.consensus

import org.mastercoin.CurrencyID
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test specification for ConsensusComparison as used in a Spock Spec
 */
class DataPipeSpec extends Specification  {

    static ConsensusComparison compareSmall1 = new ConsensusComparison(SnapshotData.small1(), SnapshotData.small1())
    static ConsensusComparison compareSmall2 = new ConsensusComparison(SnapshotData.small2(), SnapshotData.small2())

    @Unroll
    def "small1 #address #entry1 == #entry2"() {
        expect:
        entry1 == entry2

        where:
        [address, entry1, entry2] <<  compareSmall1
    }

    @Unroll
    def "small2 #address #entry1 == #entry2"() {
        expect:
        entry1 == entry2

        where:
        [address, entry1, entry2] <<  compareSmall2
    }

    @Unroll
    def "blockHeight #leftHeight == #rightHeight (#currency)"() {
        expect:
        leftHeight == rightHeight

        where:
        currency = compareSmall1.c1.currencyID
        leftHeight = compareSmall1.c1.blockHeight
        rightHeight = compareSmall1.c2.blockHeight
    }

}
