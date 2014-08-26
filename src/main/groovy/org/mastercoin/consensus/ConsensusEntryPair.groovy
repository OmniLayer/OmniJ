package org.mastercoin.consensus

/**
 * A pair of ConsensusEntries, with their Address with iteration support for Spock tests
 */
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
     *
     * @param address Bitcoin address shared by both pairs
     * @param entry1 Balance from source 1
     * @param entry2 Balance from source 2
     */
    ConsensusEntryPair(String address, ConsensusEntry entry1, ConsensusEntry entry2) {
        this.address = address
        this.entry1 = entry1
        this.entry2 = entry2
    }

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
