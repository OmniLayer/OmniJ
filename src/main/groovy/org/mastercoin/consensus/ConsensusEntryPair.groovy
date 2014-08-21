package org.mastercoin.consensus

/**
 * A pair of ConsensusEntries, with their Address with iteration support for Spock tests
 */
class ConsensusEntryPair implements Iterable<Object> {
    final String         address
    final ConsensusEntry entry1
    final ConsensusEntry entry2

    ConsensusEntryPair(String address, ConsensusEntry entry1, ConsensusEntry entry2) {
        this.address = address
        this.entry1 = entry1
        this.entry2 = entry2
    }

    @Override
    Iterator<Object> iterator() {
        return [address, entry1, entry2].iterator()
    }

}
