package foundation.omni.consensus

import foundation.omni.CurrencyID
import foundation.omni.rpc.BalanceEntry
import foundation.omni.rpc.ConsensusSnapshot
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.params.MainNetParams

/**
 * ConsensusSnapshot test data
 */
class SnapshotData {

    static String TestDataConsensusSource = "TEST DATA";

    static Address a = LegacyAddress.fromKey(MainNetParams.get(), new ECKey())
    static Address b = LegacyAddress.fromKey(MainNetParams.get(), new ECKey())
    static Address c = LegacyAddress.fromKey(MainNetParams.get(), new ECKey())

    static TreeMap<Address, BalanceEntry> empty = new TreeMap([:])
    static TreeMap<Address, BalanceEntry> small1 = new TreeMap([(a): new BalanceEntry(0.divisible, 0.divisible, 0.divisible),
                                                      (b): new BalanceEntry(1.5.divisible,  1.5.divisible,  0.divisible),
                                                      (c): new BalanceEntry(1.divisible,  0.divisible,  0.divisible)])
    static TreeMap<Address, BalanceEntry> small2 = new TreeMap([(a): new BalanceEntry(0.divisible,  0.divisible,  0.divisible),
                                                      (b): new BalanceEntry(1.5.divisible,  0.5.divisible,  0.divisible)])

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
        def snap = new ConsensusSnapshot(CurrencyID.OMNI, -1, TestDataConsensusSource, testDataURI(name), entries)
        return snap
    }

    static URI testDataURI(String name) {
        return "http://dummy.com/${name}".toURL().toURI();
    }
}
