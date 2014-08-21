package org.mastercoin.consensus

import org.mastercoin.CurrencyID

/**
 *
 * Consensus data for a specified CurrencyID at a given blockHeight.
 *
 */
class ConsensusSnapshot {
    CurrencyID  currencyID
    Long        blockHeight
    String      sourceType
    URL         sourceURL

    SortedMap<String, ConsensusEntry> entries

    SortedMap<String, ConsensusEntry> getEntriesExcluding(String address) {
        SortedMap<String, ConsensusEntry> temp = new TreeMap<String, ConsensusEntry>(entries)
        temp.remove(address)
        return temp
    }
}
