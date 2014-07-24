package org.mastercoin.consensus

import org.mastercoin.CurrencyID

/**
 * User: sean
 * Date: 7/3/14
 * Time: 11:44 AM
 */
interface ConsensusFetcher {
    ConsensusSnapshot           getConsensusSnapshot(CurrencyID currencyID);
}
