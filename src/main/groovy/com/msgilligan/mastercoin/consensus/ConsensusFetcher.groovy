package com.msgilligan.mastercoin.consensus

/**
 * User: sean
 * Date: 7/3/14
 * Time: 11:44 AM
 */
interface ConsensusFetcher {
    Map<String, ConsensusBalance> getConsensusForCurrency(Long currencyID);
}
