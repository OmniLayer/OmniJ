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
    public static final BigDecimal   MIN_VALUE = new BigDecimal(0); // Minimum value of 1 in transactions?
    public static final BigDecimal   MAX_VALUE = new BigDecimal("92233720368.54775807");
    public static final MathContext DEFAULT_CONTEXT = new MathContext(0, RoundingMode.UNNECESSARY);
    public static final int DEFAULT_SCALE = Coin.SMALLEST_UNIT_EXPONENT;
    public static final long willetsPerCoin = Coin.COIN.value; // 10^8 (Omni equivalent of satoshi unit
    private static final BigDecimal bdWilletsPerCoin = new BigDecimal(willetsPerCoin);

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
        return new OmniDivisibleValue(amount.multiply(bdWilletsPerCoin).longValueExact());
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
        BigDecimal willets = new BigDecimal(value);
        return willets.divide(bdWilletsPerCoin, DEFAULT_SCALE, RoundingMode.UNNECESSARY);
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

    @Deprecated
    public BigDecimal bigDecimalValue() {
        return numberValue();
    }

    public OmniDivisibleValue plus(OmniDivisibleValue right) {
        return OmniDivisibleValue.ofWillets(this.value + right.value);
    }

    public OmniDivisibleValue minus(OmniDivisibleValue right) {
        return OmniDivisibleValue.ofWillets(this.value - right.value);
    }

}
