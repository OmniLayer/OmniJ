package foundation.omni.consensus

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import foundation.omni.net.OmniMainNetParams
import org.bitcoinj.core.Address

/**
 * A pair of ConsensusSnapshots with comparison iterators for Spock tests
 */
@Immutable
@CompileStatic
class ConsensusComparison implements Iterable<ConsensusEntryPair>  {
    final ConsensusSnapshot c1
    final ConsensusSnapshot c2
    private Set<Address> unionAddresses = null

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
            // TODO: Do we really want to remove the exodusAddress here?
            // And what about TestNet, etc?
            unionAddresses.remove(OmniMainNetParams.get().exodusAddress)
        }
        return new PairIterator(unionAddresses.iterator())
    }

    /**
     * Iterates a ConsensusComparison pair-by-pair
     */
    class PairIterator implements Iterator<ConsensusEntryPair> {
        Iterator<Address> keyIterator

        PairIterator(Iterator<Address> keyIterator) {
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
