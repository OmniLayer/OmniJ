package foundation.omni.consensus

import groovy.transform.Immutable
import foundation.omni.OPMainNetParams

/**
 * A pair of ConsensusSnapshots with comparison iterators for Spock tests
 */
@Immutable
class ConsensusComparison implements Iterable<ConsensusEntryPair>  {
    final ConsensusSnapshot c1
    final ConsensusSnapshot c2
    private TreeSet<String> unionAddresses = null

    /**
     * Return an iterator that will iterate through the union of addresses
     * from the two ConsensusSnapshot objects, sorted by address
     * @return the iterator
     */
    @Override
    Iterator<ConsensusEntryPair> iterator() {
        if (unionAddresses == null) {
            def c1Keys = c1.entries.keySet()
            def c2Keys = c2.entries.keySet()
            unionAddresses = c1Keys + c2Keys
            unionAddresses.remove(OPMainNetParams.ExodusAddress)
        }
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
