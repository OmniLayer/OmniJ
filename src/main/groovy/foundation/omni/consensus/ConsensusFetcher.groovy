package foundation.omni.consensus

import foundation.omni.CurrencyID

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

}
