package foundation.omni.consensus

import foundation.omni.rpc.BalanceEntry
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import foundation.omni.CurrencyID
import org.bitcoinj.core.Address

/**
 * Consensus data for a specified CurrencyID at a given blockHeight.
 */
@Immutable
@CompileStatic
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
     * The URI of the server returning the consensus data
     */
    URI         sourceURI

    /**
     * Consensus entries for all addresses, sorted by address
     */
    SortedMap<Address, BalanceEntry> entries
}
