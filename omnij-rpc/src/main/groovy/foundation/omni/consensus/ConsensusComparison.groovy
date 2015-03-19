package foundation.omni.consensus

import groovy.transform.Immutable
import foundation.omni.net.OmniMainNetParams
import org.bitcoinj.core.Address

/**
 * A pair of ConsensusSnapshots with comparison iterators for Spock tests
 */
@Immutable
class ConsensusComparison implements Iterable<ConsensusEntryPair>  {
    final ConsensusSnapshot c1
    final ConsensusSnapshot c2
    private TreeSet<Address> unionAddresses = null

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
            unionAddresses.remove(OmniMainNetParams.get().exodusAddress)
        }
        return new PairIterator(unionAddresses.iterator())
    }

    /**
     * Iterates a ConsensusComparison pair-by-pair
     */
    class PairIterator implements Iterator<ConsensusEntryPair> {
        java.util.Iterator<Address> keyIterator

        PairIterator(java.util.Iterator<Address> keyIterator) {
            this.keyIterator = keyIterator
        }

        @Override
        boolean hasNext() {
            return keyIterator.hasNext()
        }

        @Override
        ConsensusEntryPair next() {
            Address key = keyIterator.next()
            ConsensusEntryPair comp = new ConsensusEntryPair(key, c1.entries[key], c2.entries[key])
            return comp
        }

        @Override
        void remove() {
            throw new UnsupportedOperationException()
        }
    }

}
