package foundation.omni;

import org.bitcoinj.core.Coin;

import java.math.BigDecimal;
import java.math.BigInteger;
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
    public static  OmniDivisibleValue MIN = OmniDivisibleValue.ofWillets(OmniValue.MIN_WILLETS);
    public static  OmniDivisibleValue MAX = OmniDivisibleValue.ofWillets(OmniValue.MAX_WILLETS);

    private static final BigDecimal willetsPerDivisibleBigDecimal = new BigDecimal(willetsPerDivisible);

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
        return new OmniDivisibleValue(amount.multiply(willetsPerDivisibleBigDecimal).longValueExact());
    }

    /**
     * Create OmniDivisibleValue from willets/internal/wire format
     *
     * @param willets number of willets
     * @return OmniIndivisibleValue equal to number of willets
     */
    public static OmniDivisibleValue ofWillets(long willets) {
        return new OmniDivisibleValue(willets);
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
        BigDecimal willets = new BigDecimal(this.willets);
        return willets.divide(willetsPerDivisibleBigDecimal, DEFAULT_SCALE, RoundingMode.UNNECESSARY);
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
        return doubleValueExact();
    }

    public BigDecimal bigDecimalValue() {
        return numberValue();
    }

    public OmniDivisibleValue plus(OmniDivisibleValue right) {
        return OmniDivisibleValue.ofWillets(this.willets + right.willets);
    }

    public OmniDivisibleValue minus(OmniDivisibleValue right) {
        return OmniDivisibleValue.ofWillets(this.willets - right.willets);
    }

    OmniDivisibleValue multiply(Integer right) {
        return OmniDivisibleValue.ofWillets(this.willets * right);
    }

    OmniDivisibleValue multiply(Long right) {
        return OmniDivisibleValue.ofWillets(this.willets * right);
    }

    OmniDivisibleValue div(Integer right) {
        return OmniDivisibleValue.ofWillets(this.willets / right);
    }

    OmniDivisibleValue div(Long right) {
        return OmniDivisibleValue.ofWillets(this.willets / right);
    }


}
