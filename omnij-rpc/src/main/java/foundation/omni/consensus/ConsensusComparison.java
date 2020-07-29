package foundation.omni.consensus;

import foundation.omni.net.OmniMainNetParams;
import foundation.omni.rpc.ConsensusSnapshot;
import org.bitcoinj.core.Address;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A pair of ConsensusSnapshots with comparison iterators for Spock tests
 */
public class ConsensusComparison implements Iterable<ConsensusEntryPair> {
    private final ConsensusSnapshot c1;
    private final ConsensusSnapshot c2;
    private transient Set<Address> unionAddresses = null;

    public ConsensusComparison(ConsensusSnapshot c1, ConsensusSnapshot c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    /**
     * Return an iterator that will iterate through the union of addresses
     * from the two ConsensusSnapshot objects, sorted by address
     *
     * @return the iterator
     */
    @Override
    public Iterator<ConsensusEntryPair> iterator() {
        if (unionAddresses == null) {
            unionAddresses = new HashSet<>();
            Set<Address> c1KeySet = c1.getEntries().keySet();
            Set<Address> c2KeySet = c2.getEntries().keySet();
            unionAddresses.addAll(c1KeySet);
            unionAddresses.addAll(c2KeySet);
            // TODO: Do we really want to remove the exodusAddress here?
            // And what about TestNet, etc?
            unionAddresses.remove(OmniMainNetParams.get().getExodusAddress());
        }

        return new PairIterator(this, unionAddresses.iterator());
    }

    public final ConsensusSnapshot getC1() {
        return c1;
    }

    public final ConsensusSnapshot getC2() {
        return c2;
    }


    /**
     * Iterates a ConsensusComparison pair-by-pair
     */
    public class PairIterator implements Iterator<ConsensusEntryPair> {
        public PairIterator(ConsensusComparison enclosing, Iterator<Address> keyIterator) {
            this.keyIterator = keyIterator;
        }

        @Override
        public boolean hasNext() {
            return keyIterator.hasNext();
        }

        @Override
        public ConsensusEntryPair next() {
            Address key = keyIterator.next();
            ConsensusEntryPair comp = new ConsensusEntryPair(key, getC1().getEntries().get(key), getC2().getEntries().get(key));
            return comp;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private Iterator<Address> keyIterator;
    }
}
