package org.mastercoin.consensus

import org.mastercoin.CurrencyID

/**
 * Interface implemented by all consensus fetching tools.
 */
interface ConsensusFetcher {
    ConsensusSnapshot           getConsensusSnapshot(CurrencyID currencyID);
}
