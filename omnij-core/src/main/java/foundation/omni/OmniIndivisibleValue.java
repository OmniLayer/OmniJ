package foundation.omni;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.text.NumberFormat;

/**
 * Numeric Value of Indivisible Omni Token
 * An indivisible token is an integer number of tokens that can't be subdivided to
 * less than one token.
 */
public final class OmniIndivisibleValue extends OmniValue {
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = 9_223_372_036_854_775_807L;
    public static final BigInteger MIN_BIGINT = BigInteger.valueOf(MIN_VALUE);
    public static final BigInteger MAX_BIGINT = BigInteger.valueOf(MAX_VALUE);
    public static final OmniIndivisibleValue ZERO = OmniIndivisibleValue.of(0);
    public static final OmniIndivisibleValue MIN = OmniIndivisibleValue.of(MIN_VALUE);
    public static final OmniIndivisibleValue MAX = OmniIndivisibleValue.of(MAX_VALUE);
    private static final NumberFormat numberFormatter = NumberFormat.getNumberInstance();

    public static OmniIndivisibleValue of(BigInteger amount) {
        checkWillettValue(amount);
        return new OmniIndivisibleValue(amount.longValueExact());
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
     * Create OmniIndivisibleValue from willetts/internal/wire format
     *
     * @param willetts number of willetts
     * @return OmniIndivisibleValue equal to number of willetts
     */
    public static OmniIndivisibleValue ofWilletts(long willetts) {
        return OmniIndivisibleValue.of(willetts);
    }

    /**
     * <p>Make sure a BigInteger value is a valid value for OmniIndivisibleValue</p>
     *
     * @param candidate value to check
     * @throws ArithmeticException if less than minimum or greater than maximum allowed value
     */
    public static void checkValue(BigInteger candidate) throws ArithmeticException {
        OmniValue.checkWillettValue(candidate);
    }

    /**
     * <p>Make sure a BigInteger value is a valid value for OmniIndivisibleValue</p>
     *
     * <p>Note: Since any positive long is valid, we just need to check that
     * it's not less than MIN_VALUE</p>
     *
     * @param candidate value to check.
     * @throws ArithmeticException if less than minimum allowed value
     */
    public static void checkValue(long candidate) throws ArithmeticException {
        OmniValue.checkWillettValue(candidate);
    }

    private OmniIndivisibleValue(long willetts) {
        super(willetts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OmniIndivisibleValue)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.willetts == ((OmniIndivisibleValue)obj).willetts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Long.toString(willetts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toPlainString() {
        return toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toJsonFormattedString() {
        return toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toFormattedString() {
        return numberFormatter.format(willetts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Long> getNumberType() {
        return Long.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValueExact() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return (double) willetts;   // Cast/conversion to double can result in rounding
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OmniIndivisibleValue round(MathContext mathContext) {
        return OmniIndivisibleValue.of(bigDecimalValue().round(mathContext).longValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long numberValue() {
        return willetts;
    }

    public BigDecimal bigDecimalValue() {
        return new BigDecimal(willetts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyType getPropertyType() {
        return PropertyType.INDIVISIBLE;
    }

    public OmniIndivisibleValue plus(OmniIndivisibleValue right) {
        return OmniIndivisibleValue.of(this.willetts + right.willetts);
    }

    public OmniIndivisibleValue minus(OmniIndivisibleValue right) {
        return OmniIndivisibleValue.of(this.willetts - right.willetts);
    }
}
