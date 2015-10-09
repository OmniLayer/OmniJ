package foundation.omni.consensus

import foundation.omni.CurrencyID
import foundation.omni.rpc.BalanceEntry
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

    static TreeMap<Address, BalanceEntry> empty = new TreeMap([:])
    static TreeMap<Address, BalanceEntry> small1 = new TreeMap([(a): new BalanceEntry(0, 0),
                                                      (b): new BalanceEntry(1.5,  1.5),
                                                      (c): new BalanceEntry(1,  0)])
    static TreeMap<Address, BalanceEntry> small2 = new TreeMap([(a): new BalanceEntry(0,  0),
                                                      (b): new BalanceEntry(1.5,  0.5)])

    static ConsensusSnapshot empty() {
        return createSnapshot("empty", empty)
    }

    static ConsensusSnapshot small1() {
        return createSnapshot("small1", small1)
    }

    static ConsensusSnapshot small2() {
        return createSnapshot("small2", small2)
    }

    static ConsensusSnapshot createSnapshot(String name, SortedMap<Address, BalanceEntry> entries) {
        def snap = new ConsensusSnapshot(CurrencyID.MSC, -1, TestDataConsensusSource, testDataURI(name), entries)
        return snap
    }

    static URI testDataURI(String name) {
        return "http://dummy.com/${name}".toURL().toURI();
    }
}
