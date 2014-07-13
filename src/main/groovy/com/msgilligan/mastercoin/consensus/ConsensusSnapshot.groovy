package com.msgilligan.mastercoin.consensus

import groovy.transform.Canonical
import org.mastercoin.CurrencyID

/**
 * User: sean
 * Date: 7/9/14
 * Time: 1:12 PM
 */
class ConsensusSnapshot {
    Long    currencyID
    Long    blockHeight
    String  sourceType
    URL     sourceURL

    SortedMap<String, ConsensusEntry> entries
}
