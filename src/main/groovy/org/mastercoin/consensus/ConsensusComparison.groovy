package org.mastercoin.consensus

import org.mastercoin.MPMainNetParams
import org.mastercoin.Mastercoin

/**
 * User: sean
 * Date: 7/12/14
 * Time: 3:40 PM
 */
class ConsensusComparison implements Iterable<ConsensusEntryPair>  {
    final ConsensusSnapshot c1
    final ConsensusSnapshot c2
//    private TreeSet<String> intersectingAddresses
    private TreeSet<String> unionAddresses
//    private TreeSet<String> extraInFirst
//    private TreeSet<String> extraInSecond

    ConsensusComparison(ConsensusSnapshot c1, ConsensusSnapshot c2) {
        this.c1 = c1
        this.c2 = c2
        def c1Keys = c1.entries.keySet()
        def c2Keys = c2.entries.keySet()
//        intersectingAddresses = c1Keys.intersect(c2Keys)
//        intersectingAddresses.remove(Mastercoin.ExodusAddress)
//        extraInFirst = c1Keys - c2Keys
//        extraInSecond = c2Keys - c1Keys
        unionAddresses = c1Keys + c2Keys
        unionAddresses.remove(MPMainNetParams.ExodusAddress)
    }

//    List<String> addressIntersection() {
//        return intersectingAddresses.toList()
//    }
//
//    List<String> extraAddressesInFirst() {
//        return extraInFirst.toList()
//    }
//
//    List<String> extraAddressesInSecond() {
//        return extraInSecond.toList()
//    }
//
//    IterablePairs intersection() {
//        return new IterablePairs(intersectingAddresses)
//    }
//
//    IterablePairs union() {
//        return new IterablePairs(unionAddresses)
//    }

    @Override
    Iterator<ConsensusEntryPair> iterator() {
        return new PairIterator(unionAddresses.iterator())
    }

    class IterablePairs implements Iterable<ConsensusEntryPair> {
        private Iterable<String> keys

        IterablePairs(Iterable<String> keys) {
            this.keys = keys
        }

        @Override
        Iterator<ConsensusEntryPair> iterator() {
            return new PairIterator(keys.iterator())
        }
    }

    class PairIterator implements Iterator<ConsensusEntryPair> {
        java.util.Iterator<String> keyIterator

        PairIterator(java.util.Iterator<String> keyIterator) {
            this.keyIterator = keyIterator
        }

        @Override
        boolean hasNext() {
            return keyIterator.hasNext()
        }

        @Override
        ConsensusEntryPair next() {
            String key = keyIterator.next()
            ConsensusEntryPair comp = new ConsensusEntryPair(key, c1.entries[key], c2.entries[key])
            return comp
        }

        @Override
        void remove() {
            throw new UnsupportedOperationException()
        }
    }

}
