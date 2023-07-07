package foundation.omni.consensus;

import foundation.omni.BalanceEntry;
import org.bitcoinj.base.Address;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A pair of ConsensusEntries, with their Address with iteration support for Spock tests
 */
public class ConsensusEntryPair implements Iterable<Object> {
    /**
     * Bitcoin Address
     */
    private final Address address;
    /**
     * Consensus Entry from first source
     */
    private final BalanceEntry entry1;
    /**
     * Consensus Entry from second source
     */
    private final BalanceEntry entry2;

    public ConsensusEntryPair(Address address, BalanceEntry entry1, BalanceEntry entry2) {
        this.address = address;
        this.entry1 = entry1;
        this.entry2 = entry2;
    }

    /**
     * Useful for Spock tests
     *
     * @return an iterator
     */
    @Override
    public Iterator<Object> iterator() {
        return new ArrayList<>(Arrays.asList(address, entry1, entry2)).iterator();
    }


    public final Address getAddress() {
        return address;
    }

    public final BalanceEntry getEntry1() {
        return entry1;
    }

    public final BalanceEntry getEntry2() {
        return entry2;
    }

}
