package foundation.omni.rpc;

import foundation.omni.CurrencyID;
import foundation.omni.rpc.BalanceEntry;
import org.bitcoinj.core.Address;

import java.net.URI;
import java.util.SortedMap;

/**
 * Consensus data for a specified CurrencyID at a given blockHeight.
 * Should really be in the package foundation.omni.consensus, but is here as a workaround
 * to what appears to be a Groovy joint-compilation issue.
 */
public class ConsensusSnapshot {
    /**
     * The currency ID
     */
    private final CurrencyID currencyID;

    /**
     * Bitcoin block height (aka blockCount) at time of snapshot
     */
    private final int blockHeight;

    /**
     * A string identifying the source of the consensus data
     */
    private final String sourceType;

    /**
     * The URI of the server returning the consensus data
     */
    private final URI sourceURI;

    /**
     * Consensus entries for all addresses, sorted by address
     */
    private final SortedMap<Address, BalanceEntry> entries;

    public final CurrencyID getCurrencyID() {
    return currencyID;
    }

    public final int getBlockHeight() {
        return blockHeight;
    }

    public final String getSourceType() {
        return sourceType;
    }

    public final URI getSourceURI() {
        return sourceURI;
    }

    public final SortedMap<Address, BalanceEntry> getEntries() {
        return entries;
    }

    /**
     * ConsensusSnapshot from SortedMap and parameters
     * @param currencyID currency id
     * @param blockHeight blockheight of snapshot
     * @param sourceType type of server returning the snapshot
     * @param sourceURI URI of server returning the snapshot
     * @param entries map of balance entries
     */
    public ConsensusSnapshot(CurrencyID currencyID, int blockHeight, String sourceType, URI sourceURI, SortedMap<Address, BalanceEntry> entries)
    {

        this.currencyID = currencyID;
        this.blockHeight = blockHeight;
        this.sourceType = sourceType;
        this.sourceURI = sourceURI;
        this.entries = entries;
    }

    /**
     * ConsensusSnapshot from SortedMap and parameters
     * @param currencyID currency id
     * @param blockHeight blockheight of snapshot
     * @param sourceType type of server returning the snapshot
     * @param sourceURI URI of server returning the snapshot
     * @param entries map of balance entries
     * @deprecated Use constructor with int blockHeight
     */
    @Deprecated
    public ConsensusSnapshot(CurrencyID currencyID, Long blockHeight, String sourceType, URI sourceURI, SortedMap<Address, BalanceEntry> entries)
    {
        this(currencyID, blockHeight.intValue(), sourceType, sourceURI, entries);
    }
}
