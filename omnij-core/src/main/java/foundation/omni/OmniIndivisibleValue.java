package foundation.omni;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Numeric Value of Indivisible Omni Token
 * An indivisible token is an integer number of tokens that can't be subdivided to
 * less than one token.
 */
public final class OmniIndivisibleValue extends OmniValue {
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = 9223372036854775807L;
    public static final BigInteger MIN_BIGINT = BigInteger.valueOf(MIN_VALUE);
    public static final BigInteger MAX_BIGINT = BigInteger.valueOf(MAX_VALUE);
    public static final OmniIndivisibleValue MIN = OmniIndivisibleValue.of(MIN_VALUE);
    public static final OmniIndivisibleValue MAX = OmniIndivisibleValue.of(MAX_VALUE);


    public static OmniIndivisibleValue of(BigInteger amount) {
        checkValue(amount);
        return new OmniIndivisibleValue(amount.intValue());
    }

    /**
     * Create OmniDivisibleValue of the specified amount
     * @param amount Number of Omni tokens
     * @return OmniDivisibleValue representing amount tokens
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

    private OmniIndivisibleValue(long willets) {
        super(willets);
    }

    @Override
    public Class<Long> getNumberType() {
        return Long.class;
    }

    @Override
    public OmniIndivisibleValue round(MathContext mathContext) {
        return OmniIndivisibleValue.of(asBigDecimal().round(mathContext).longValue());
    }

    @Override
    public Long numberValue() {
        return willets;
    }

    public BigDecimal bigDecimalValue() {
        return asBigDecimal();
    }

    private BigDecimal asBigDecimal() {
        return new BigDecimal(willets);
    }

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.INDIVISIBLE;
    }

    public OmniIndivisibleValue plus(OmniIndivisibleValue right) {
        return OmniIndivisibleValue.of(this.willets + right.willets);
    }

    public OmniIndivisibleValue minus(OmniIndivisibleValue right) {
        return OmniIndivisibleValue.of(this.willets - right.willets);
    }

}
