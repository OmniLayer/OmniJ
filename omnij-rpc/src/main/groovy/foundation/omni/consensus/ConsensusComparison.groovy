package foundation.omni.consensus

import foundation.omni.rpc.ConsensusSnapshot
import groovy.transform.CompileStatic
import foundation.omni.net.OmniMainNetParams
import org.bitcoinj.core.Address

/**
 * A pair of ConsensusSnapshots with comparison iterators for Spock tests
 */
@CompileStatic
class ConsensusComparison implements Iterable<ConsensusEntryPair>  {
    final ConsensusSnapshot c1
    final ConsensusSnapshot c2
    private transient Set<Address> unionAddresses = null

    ConsensusComparison(ConsensusSnapshot c1, ConsensusSnapshot c2) {
        this.c1 = c1
        this.c2 = c2
    }
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
        /* Use temp variable to workaround GROOVY-8590
         * https://issues.apache.org/jira/browse/GROOVY-8590
         */
        def temp = new PairIterator(unionAddresses.iterator())
        return temp;
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
