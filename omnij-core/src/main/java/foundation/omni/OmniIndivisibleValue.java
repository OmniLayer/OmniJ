package foundation.omni;

import java.math.BigInteger;

/**
 * Numeric Value of Indivisible Omni Token
 * An indivisible token is an integer number of tokens that can't be subdivided to
 * less than one token.
 */
public final class OmniIndivisibleValue extends OmniValue {
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = 9223372036854775807L;

    /**
     * Create OmniDivisibleValue of the specified amount
     * @param amount Number of Omni tokens
     * @return
     */
    public static OmniIndivisibleValue of(long amount) {
        return new OmniIndivisibleValue(amount);
    }

    /**
     * Create OmniIndivisibleValue from willets/internal/wire format
     *
     * @param willets number of willets
     * @return OmniIndivisibleValue equal to number of willets
     */
    public static OmniIndivisibleValue ofWillets(long willets) {
        return OmniIndivisibleValue.of(willets);
    }

    private OmniIndivisibleValue(long value) {
        super(value);
    }

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.INDIVISIBLE;
    }

}
