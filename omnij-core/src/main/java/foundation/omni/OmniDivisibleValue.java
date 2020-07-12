package foundation.omni;

import org.bitcoinj.core.Coin;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Numeric Value of Divisible Omni Token
 * An Omni Divisible token is typically represented to the user as a decimal amount and hence
 * you can have a fractional number of tokens. Internally it uses the same format as a divisible token.
 *
 */
public final class OmniDivisibleValue extends OmniValue {
    public static final MathContext DEFAULT_CONTEXT = new MathContext(0, RoundingMode.UNNECESSARY);
    public static final int DEFAULT_SCALE = Coin.SMALLEST_UNIT_EXPONENT;
    public static final BigDecimal   MIN_VALUE = BigDecimal.ZERO;
    public static final BigDecimal   MAX_VALUE = new BigDecimal("92233720368.54775807");
    public static final long         MIN_INT_VALUE = 0L;
    public static final long         MAX_INT_VALUE = 92233720368L;
    public static  OmniDivisibleValue MIN = OmniDivisibleValue.ofWilletts(OmniValue.MIN_WILLETTS);
    public static  OmniDivisibleValue MAX = OmniDivisibleValue.ofWilletts(OmniValue.MAX_WILLETTS);

    private static final BigDecimal willettsPerDivisibleBigDecimal = new BigDecimal(willettsPerDivisible);

    /**
     * Create OmniDivisibleValue of the specified amount
     * @param amount Number of Omni tokens
     * @return OmniDivisibleValue representing amount
     */
    public static OmniDivisibleValue of(long amount) {
        return OmniDivisibleValue.of(BigDecimal.valueOf(amount));
    }

    /**
     * Create OmniDivisibleValue of the specified amount
     * @param amount Number of Omni tokens
     * @return OmniDivisibleValue representing amount
     */
    public static OmniDivisibleValue of(BigDecimal amount) {
        return new OmniDivisibleValue(amount.multiply(willettsPerDivisibleBigDecimal).longValueExact());
    }

    /**
     * Create OmniDivisibleValue from willetts/internal/wire format
     *
     * @param willetts number of willetts
     * @return OmniIndivisibleValue equal to number of willetts
     */
    public static OmniDivisibleValue ofWilletts(long willetts) {
        return new OmniDivisibleValue(willetts);
    }

    /**
     * <p>Make sure a BigDecimal value is a valid value for OmniDivisibleValue</p>
     *
     * @param candidate value to check
     * @throws ArithmeticException if less than minimum or greater than maximum allowed value
     */
    public static void checkValue(BigDecimal candidate) throws ArithmeticException {
        if (candidate.compareTo(MIN_VALUE) < 0) {
            throw new ArithmeticException();
        }
        if (candidate.compareTo(MAX_VALUE) > 0) {
            throw new ArithmeticException();
        }
    }

    /**
     * <p>Make sure a long value is a valid value for OmniDivisibleValue</p>
     *
     * @param candidate value to check.
     * @throws ArithmeticException if less than minimum or greater than maximum allowed value
     */
    public static void checkValue(long candidate) throws ArithmeticException {
        if (candidate < MIN_INT_VALUE) {
            throw new ArithmeticException();
        }
        if (candidate > MAX_INT_VALUE) {
            throw new ArithmeticException();
        }
    }

    private OmniDivisibleValue(long value) {
        super(value);
    }

    @Override
    public PropertyType getPropertyType() {
        return PropertyType.DIVISIBLE;
    }

    @Override
    public Class<BigDecimal> getNumberType() {
        return BigDecimal.class;
    }

    @Override
    public BigDecimal numberValue() {
        return bigDecimalValue();
    }

    @Override
    public int getPrecision() {
        return DEFAULT_CONTEXT.getPrecision();
    }

    @Override
    public int getScale() {
        return DEFAULT_SCALE;
    }

    @Override
    public int intValueExact() {
        return numberValue().intValueExact();
    }

    @Override
    public long longValueExact() {
        return numberValue().longValueExact();
    }

    @Override
    public double doubleValueExact() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public OmniDivisibleValue round(MathContext mathContext) {
        return OmniDivisibleValue.of(numberValue().round(mathContext));
    }

    @Override
    public long getAmountFractionNumerator() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public long getAmountFractionDenominator() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public byte byteValue() {
        return numberValue().byteValueExact();
    }

    @Override
    public short shortValue() {
        return numberValue().shortValueExact();
    }

    @Override
    public int intValue() {
        return intValueExact();
    }

    @Override
    public long longValue() {
        return longValueExact();
    }

    @Override
    public float floatValue() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public double doubleValue() {
        return bigDecimalValue().doubleValue(); // Warning: Converting BigDecimal to Double can result in rounding
    }

    public BigDecimal bigDecimalValue() {
        BigDecimal willetts = new BigDecimal(this.willetts);
        // TODO: Consider moving the decimal point rather than dividing
        return willetts.divide(willettsPerDivisibleBigDecimal, DEFAULT_SCALE, RoundingMode.UNNECESSARY);
    }

    public OmniDivisibleValue plus(OmniDivisibleValue right) {
        return OmniDivisibleValue.ofWilletts(this.willetts + right.willetts);
    }

    public OmniDivisibleValue minus(OmniDivisibleValue right) {
        return OmniDivisibleValue.ofWilletts(this.willetts - right.willetts);
    }

    OmniDivisibleValue multiply(Integer right) {
        return OmniDivisibleValue.ofWilletts(this.willetts * right);
    }

    OmniDivisibleValue multiply(Long right) {
        return OmniDivisibleValue.ofWilletts(this.willetts * right);
    }

    OmniDivisibleValue div(Integer right) {
        return OmniDivisibleValue.ofWilletts(this.willetts / right);
    }

    OmniDivisibleValue div(Long right) {
        return OmniDivisibleValue.ofWilletts(this.willetts / right);
    }


}
