package foundation.omni.consensus

import foundation.omni.CurrencyID
import foundation.omni.rpc.SmartPropertyListInfo

/**
 * Interface implemented by all consensus fetching tools.
 */
interface ConsensusFetcher {
    /**
     * Fetch a consensus snapshot for a currencyID
     * @param currencyID The currency to get consensus data for
     * @return Consensus data for all addresses owning currencyID
     */
    ConsensusSnapshot           getConsensusSnapshot(CurrencyID currencyID)

    /**
     * Fetch the current block height
     *
     * @return The current blockheight of the remote consensus server
     */
    Integer currentBlockHeight()

    /**
     * Get a list of properties
     *
     * Should there be a property snapshot that includes a blockheight?
     *
     * @return A list of property objects
     */
    List<SmartPropertyListInfo> listProperties()
}
