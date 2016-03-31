package foundation.omni.rpc;

import foundation.omni.CurrencyID;

import java.util.List;

/**
 * Interface implemented by all consensus fetching tools.
 * Should really be in the package foundation.omni.consensus, but is here as a workaround
 * to what appears to be a Groovy joint-compilation issue.
 */
public interface ConsensusFetcher {
    /**
     * Fetch a consensus snapshot for a currencyID
     *
     * @param currencyID The currency to get consensus data for
     * @return Consensus data for all addresses owning currencyID
     */
    ConsensusSnapshot getConsensusSnapshot(CurrencyID currencyID);

    /**
     * Fetch the current block height
     *
     * @return The current blockheight of the remote consensus server
     */
    Integer currentBlockHeight();

    /**
     * Get a list of properties
     *
     * Should there be a property snapshot that includes a blockheight?
     *
     * @return A list of property objects
     */
    List<SmartPropertyListInfo> listProperties();
}
