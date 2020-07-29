package foundation.omni.dsl.categories

import foundation.omni.OmniValue
import groovy.transform.CompileStatic

import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniIndivisibleValue


/**
 * Convenience Category for converting Numbers to an OmniValue class
 *
 */
@CompileStatic
@Category(Number)
class NumberCategory {
    /**
     * Treat number as a decimal, divisible amount of an Omni currency
     *
     * @return a OmniDivisibleValue object
     */
    public OmniDivisibleValue getDivisible() {
        return OmniDivisibleValue.of(asDivisible(this))
    }

    /**
     * Treat number as an integer, indivisible amount of an Omni currency
     *
     * @return a OmniIndivisibleValue object
     */
    public OmniIndivisibleValue getIndivisible() {
        return OmniIndivisibleValue.of(asIndivisible(this))
    }

    static BigDecimal asDivisible(Number self) {
        switch(self) {
            case BigDecimal:    return self
            case BigInteger:    return new BigDecimal((BigInteger)self)
            default:            return new BigDecimal(self.longValue())
        }
    }

    static long asIndivisible(Number self) {
        switch(self) {
            case BigDecimal:    return ((BigDecimal) self).longValueExact()
            case BigInteger:    return ((BigInteger) self).longValueExact()
            default:            return self.longValue()
        }
    }
}
