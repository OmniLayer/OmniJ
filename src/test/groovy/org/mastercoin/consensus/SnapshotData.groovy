package org.mastercoin.consensus

import org.mastercoin.CurrencyID

/**
 * ConsensusSnapshot test data
 */
class SnapshotData {

    static TreeMap<String, ConsensusEntry> empty = [:]
    static TreeMap<String, ConsensusEntry> small1 = [a: new ConsensusEntry(balance: 0, reserved: 0),
                                                       b: new ConsensusEntry(balance: 1.5G, reserved: 1.5G),
                                                       c: new ConsensusEntry(balance: 1G, reserved: 0)]
    static TreeMap<String, ConsensusEntry> small2 = [a: new ConsensusEntry(balance: 0, reserved: 0),
                                                       b: new ConsensusEntry(balance: 1.5G, reserved: 0.5G)]

    static ConsensusSnapshot empty() {
        return createSnapshot("empty", empty)
    }

    static ConsensusSnapshot small1() {
        return createSnapshot("small1", small1)
    }

    static ConsensusSnapshot small2() {
        return createSnapshot("small2", small2)
    }

    static ConsensusSnapshot createSnapshot(String name, SortedMap<String, ConsensusEntry> entries) {
        def snap = new ConsensusSnapshot()
        snap.sourceURL = "http://dummy.com/${name}".toURL()
        snap.entries = entries
        snap.blockHeight = -1
        snap.currencyID = CurrencyID.MSC
        snap.sourceType = "TEST DATA"
        return snap
    }
}
