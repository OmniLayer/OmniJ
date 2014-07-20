package org.mastercoin.consensus

import groovy.transform.Immutable
import groovy.transform.ToString

/**
 * User: sean
 * Date: 7/3/14
 * Time: 12:46 PM
 */
@Immutable
@ToString(includePackage=false, includeNames=true)
class ConsensusEntry {
    BigDecimal  balance
    BigDecimal  reserved
}
