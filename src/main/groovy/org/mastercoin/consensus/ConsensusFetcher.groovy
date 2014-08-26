package org.mastercoin.consensus

import org.mastercoin.CurrencyID

/**
 * Interface implemented by all consensus fetching tools.
 */
interface ConsensusFetcher {
    /**
     * Fetch a consensus snapshot for a currencyID
     * @param currencyID The currency to get consensus data for
     * @return Consensus data for all addresses owning currencyID
     */
    ConsensusSnapshot           getConsensusSnapshot(CurrencyID currencyID);
}
