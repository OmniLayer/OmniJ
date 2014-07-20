package org.mastercoin.consensus

/**
 * User: sean
 * Date: 7/19/14
 * Time: 1:58 PM
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
