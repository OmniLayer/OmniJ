package org.mastercoin.consensus

import spock.lang.Specification

import static org.mastercoin.CurrencyID.*

/**
 *
 */
class ConsensusComparisonSpec extends Specification {
    static ConsensusComparison compareSmall1 = new ConsensusComparison(SnapshotData.small1(), SnapshotData.small1())

    def "is immutable"() {
        when: "we try to set one of the properties"
        compareSmall1.c1 = SnapshotData.small2()

        then: "an exception is thrown and value isn't changed"
        ReadOnlyPropertyException e = thrown()
    }

    def "allows enumeration and comparison of all entries via an iterator"() {
        setup: "init vars to verify all entries compared with no mismatches"
        def foundMismatch = false;
        def compareCount = 0

        when: "we compare each entry in identical Snapshots"
        for (pair in compareSmall1) {
            compareCount++
            if (pair.entry1 != pair.entry2) {
                foundMismatch = true;
            }
        }

        then: "we find no differences"
        foundMismatch == false
        compareCount == compareSmall1.c1.entries.size()
        compareCount == compareSmall1.c2.entries.size()
    }

}
