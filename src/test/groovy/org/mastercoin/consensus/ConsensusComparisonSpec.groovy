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
}
