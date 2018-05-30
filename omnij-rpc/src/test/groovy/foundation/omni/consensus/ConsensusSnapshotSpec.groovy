package foundation.omni.consensus

import foundation.omni.rpc.ConsensusSnapshot
import spock.lang.Specification

import static foundation.omni.CurrencyID.*

/**
 * Test Specification for ConsensusSnapshot
 */
class ConsensusSnapshotSpec extends Specification {
    def "can be created from test data"() {
        when:
        def snap = new ConsensusSnapshot(OMNI, -1, SnapshotData.TestDataConsensusSource, SnapshotData.testDataURI("empty"), SnapshotData.empty)

        then:
        snap.currencyID == OMNI
        snap.blockHeight == -1
        snap.sourceType == SnapshotData.TestDataConsensusSource
        snap.sourceURI == SnapshotData.testDataURI("empty")
        snap.entries == SnapshotData.empty
    }

    def "is immutable"() {
        when: "we try to set one of the properties"
        def snap = new ConsensusSnapshot(OMNI, -1, SnapshotData.TestDataConsensusSource, SnapshotData.testDataURI("empty"), SnapshotData.empty)
        snap.currencyID = TOMNI

        then: "an exception is thrown and value isn't changed"
        ReadOnlyPropertyException e = thrown()
        snap.currencyID == OMNI
    }
}
