package foundation.omni;

import java.math.BigInteger;

/**
 * Numeric Value of Indivisible Omni Token
 */
public class OmniIndivisibleValue extends OmniValue {
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = 9223372036854775807L;

    /**
     * Create OmniIndivisibleValue from willets/internal/wire format
     *
     * @param willets number of willets
     * @return OmniIndivisibleValue equal to number of willets
     */
    public static OmniIndivisibleValue fromWillets(long willets) {
        return new OmniIndivisibleValue(willets);
    }

    public OmniIndivisibleValue(long value) {
        super(value);
    }
    public OmniIndivisibleValue(BigInteger value) {
        super(value);
    }
}
