package org.mastercoin.consensus

import org.mastercoin.CurrencyID

/**
 *
 * Consensus data for a specified CurrencyID at a given blockHeight.
 *
 */
class ConsensusSnapshot {
    /**
     * The currency ID
     */
    CurrencyID  currencyID
    /**
     * Bitcoin block height (aka blockCount) at time of snapshot
     */
    Long        blockHeight
    /**
     * A string identifying the source of the consensus data
     */
    String      sourceType
    /**
     * The URL of the server returning the consensus data
     */
    URL         sourceURL

    /**
     * Consensus entries for all addresses, sorted by address
     */
    SortedMap<String, ConsensusEntry> entries

    /**
     * Return all entries excluding a single address
     * @param address Address to exclude (e.g. Exodus address)
     * @return A map of consensus entries sorted by address
     */
    SortedMap<String, ConsensusEntry> getEntriesExcluding(String address) {
        SortedMap<String, ConsensusEntry> temp = new TreeMap<String, ConsensusEntry>(entries)
        temp.remove(address)
        return temp
    }
}
