package foundation.omni.consensus

import foundation.omni.rpc.BalanceEntry
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import org.bitcoinj.core.Address

/**
 * A pair of ConsensusEntries, with their Address with iteration support for Spock tests
 */
@Immutable(knownImmutableClasses = [BalanceEntry])
@CompileStatic
class ConsensusEntryPair implements Iterable<Object> {
    /**
     * Bitcoin Address
     */
    final Address         address
    /**
     * Consensus Entry from first source
     */
    final BalanceEntry entry1
    /**
     * Consensus Entry from second source
     */
    final BalanceEntry entry2

    /**
     * Useful for Spock tests
     *
     * @return
     */
    @Override
    Iterator<Object> iterator() {
        return [address, entry1, entry2].iterator()
    }

}
