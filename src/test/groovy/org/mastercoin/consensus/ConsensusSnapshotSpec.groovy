package org.mastercoin.consensus

import spock.lang.Specification

import static org.mastercoin.CurrencyID.*

/**
 * Test Specification for ConsensusSnapshot
 */
class ConsensusSnapshotSpec extends Specification {
    def "can be created from test data"() {
        when:
        def snap = new ConsensusSnapshot(MSC, -1, SnapshotData.TestDataConsensusSource, SnapshotData.testDataURI("empty"), SnapshotData.empty)

        then:
        snap.currencyID == MSC
        snap.blockHeight == -1
        snap.sourceType == SnapshotData.TestDataConsensusSource
        snap.sourceURI == SnapshotData.testDataURI("empty")
        snap.entries == SnapshotData.empty
    }

    def "is immutable"() {
        when: "we try to set one of the properties"
        def snap = new ConsensusSnapshot(MSC, -1, SnapshotData.TestDataConsensusSource, SnapshotData.testDataURI("empty"), SnapshotData.empty)
        snap.currencyID = TMSC

        then: "an exception is thrown and value isn't changed"
        ReadOnlyPropertyException e = thrown()
        snap.currencyID == MSC
    }
}
