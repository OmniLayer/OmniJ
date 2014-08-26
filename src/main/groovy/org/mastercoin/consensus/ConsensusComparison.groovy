package org.mastercoin.consensus

import org.mastercoin.MPMainNetParams
import org.mastercoin.Mastercoin

/**
 * A pair of ConsensusSnapshots with comparison iterators for Spock tests
 */
class ConsensusComparison implements Iterable<ConsensusEntryPair>  {
    final ConsensusSnapshot c1
    final ConsensusSnapshot c2
    private TreeSet<String> unionAddresses

    /**
     * Constructs a ConsensusComparison from two ConsensusSnapshot objects
     * @param c1 Snapshot from source #1
     * @param c2 Snapshot from source #2
     */
    ConsensusComparison(ConsensusSnapshot c1, ConsensusSnapshot c2) {
        this.c1 = c1
        this.c2 = c2
        def c1Keys = c1.entries.keySet()
        def c2Keys = c2.entries.keySet()
        unionAddresses = c1Keys + c2Keys
        unionAddresses.remove(MPMainNetParams.ExodusAddress)
    }

    /**
     * Return an iterator that will iterate through the union of addresses
     * from the two ConsensusSnapshot objects, sorted by address
     * @return the iterator
     */
    @Override
    Iterator<ConsensusEntryPair> iterator() {
        return new PairIterator(unionAddresses.iterator())
    }


    /**
     * Iterates a ConsensusComparison pair-by-pair
     */
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
