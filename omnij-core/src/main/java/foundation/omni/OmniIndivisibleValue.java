package foundation.omni;

import java.math.BigInteger;

/**
 *
 */
public class OmniIndivisibleValue extends OmniValue {
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = 9223372036854775807L;

    public OmniIndivisibleValue(long value) {
        super(value);
    }
    public OmniIndivisibleValue(BigInteger value) {
        super(value);
    }
}
