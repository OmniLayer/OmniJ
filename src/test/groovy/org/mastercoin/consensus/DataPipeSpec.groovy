package org.mastercoin.consensus

import org.mastercoin.CurrencyID
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * User: sean
 * Date: 7/9/14
 * Time: 4:40 PM
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

}
