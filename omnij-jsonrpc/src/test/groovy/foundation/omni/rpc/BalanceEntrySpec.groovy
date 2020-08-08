package foundation.omni.rpc

import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniIndivisibleValue
import foundation.omni.PropertyType
import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 */
class BalanceEntrySpec extends Specification {
    @Unroll
    def "Test constructor and getters (divisible) "(BigDecimal balance, BigDecimal reserved, BigDecimal frozen) {
        when:
        def balanceEntry = new BalanceEntry(
                OmniDivisibleValue.of(balance, PropertyType.DIVISIBLE),
                OmniDivisibleValue.of(reserved, PropertyType.DIVISIBLE),
                OmniDivisibleValue.of(frozen, PropertyType.DIVISIBLE)
        )

        then:
        balanceEntry.balance.bigDecimalValue() == balance
        balanceEntry.reserved.bigDecimalValue() == reserved
        balanceEntry.frozen.bigDecimalValue() == frozen ?: 0
        BalanceEntry.totalBalance(balanceEntry) == balance + reserved


        where:
        balance | reserved | frozen
        0 | 0 | 0
        1 | 1 | 1
        OmniDivisibleValue.MIN_VALUE | 0 | 0
        OmniDivisibleValue.MAX_VALUE | 0 | 0
    }

    @Unroll
    def "Test constructor and getters (indivisible) "(long balance, long reserved, long frozen) {
        when:
        def balanceEntry = new BalanceEntry(
                OmniIndivisibleValue.of(balance),
                OmniIndivisibleValue.of(reserved),
                OmniIndivisibleValue.of(frozen)
        )

        then:
        balanceEntry.balance.longValueExact() == balance
        balanceEntry.reserved.longValueExact() == reserved
        balanceEntry.frozen.longValueExact() == frozen ?: 0
        BalanceEntry.totalBalance(balanceEntry) == balance + reserved


        where:
        balance | reserved | frozen
        0 | 0 | 0
        1 | 1 | 1
        OmniIndivisibleValue.MIN_VALUE | 0 | 0
        OmniIndivisibleValue.MAX_VALUE | 0 | 0
    }

}
