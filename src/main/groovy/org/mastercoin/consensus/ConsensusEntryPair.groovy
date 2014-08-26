package org.mastercoin.consensus

import groovy.transform.Immutable

/**
 * A pair of ConsensusEntries, with their Address with iteration support for Spock tests
 */
@Immutable
class ConsensusEntryPair implements Iterable<Object> {
    /**
     * Bitcoin Address
     */
    final String         address
    /**
     * Consensus Entry from first source
     */
    final ConsensusEntry entry1
    /**
     * Consensus Entry from second source
     */
    final ConsensusEntry entry2

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
