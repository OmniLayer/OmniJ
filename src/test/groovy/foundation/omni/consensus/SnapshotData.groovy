package foundation.omni.consensus

import foundation.omni.CurrencyID
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.MainNetParams

/**
 * ConsensusSnapshot test data
 */
class SnapshotData {

    static String TestDataConsensusSource = "TEST DATA";

    static Address a = new ECKey().toAddress(MainNetParams.get())
    static Address b = new ECKey().toAddress(MainNetParams.get())
    static Address c = new ECKey().toAddress(MainNetParams.get())

    static TreeMap<Address, ConsensusEntry> empty = new TreeMap([:])
    static TreeMap<Address, ConsensusEntry> small1 = new TreeMap([(a): new ConsensusEntry(balance: 0, reserved: 0),
                                                      (b): new ConsensusEntry(balance: 1.5G, reserved: 1.5G),
                                                      (c): new ConsensusEntry(balance: 1G, reserved: 0)])
    static TreeMap<Address, ConsensusEntry> small2 = new TreeMap([(a): new ConsensusEntry(balance: 0, reserved: 0),
                                                      (b): new ConsensusEntry(balance: 1.5G, reserved: 0.5G)])

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
        def snap = new ConsensusSnapshot(CurrencyID.MSC, -1, TestDataConsensusSource, testDataURI(name), entries)
        return snap
    }

    static URI testDataURI(String name) {
        return "http://dummy.com/${name}".toURL().toURI();
    }
}
