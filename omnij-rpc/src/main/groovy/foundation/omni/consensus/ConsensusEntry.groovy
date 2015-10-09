package foundation.omni.consensus

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.ToString

/**
 * Consensus data for a particular address at a given blockHeight for a particular CurrencyID.
 *
 * Note: Due to changes in the RPCs this structure is now very similar to BalanceEntry and the
 * two should probably be combined. -- Done - BalanceEntry replaces this class.
 *
 * @deprecated use BalanceEntry
 */
@Immutable
@ToString(includePackage=false, includeNames=true)
@CompileStatic
@Deprecated
class ConsensusEntry implements Iterable<BigDecimal>  {
    /**
     * total balance
     */
    BigDecimal  balance
    /**
     * reserved funds (forOffer + forAccept)
     */
    BigDecimal  reserved

    /**
     * Useful for Spock tests
     *
     * @return an iterator that will iterate the two BigDecimal fields
     */
    @Override
    Iterator<BigDecimal> iterator() {
        return [balance, reserved].iterator()
    }

}
