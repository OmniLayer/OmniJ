package com.msgilligan.mastercoin.consensus

import groovy.transform.Canonical

/**
 * User: sean
 * Date: 7/3/14
 * Time: 12:46 PM
 */
@Canonical
class ConsensusEntry {
    String      address
    BigDecimal  balance
    BigDecimal  reserved
}
