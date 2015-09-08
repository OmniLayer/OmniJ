package foundation.omni;

import org.bitcoinj.core.Coin;

import javax.money.NumberValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Numeric Value of Divisible Omni Token
 * An Omni Divisible token is typically represented to the user as a decimal amount and hence
 * you can have a fractional number of tokens. Internally it uses the same format as a divisible token.
 *
 */
public final class OmniDivisibleValue extends OmniValue {
    public static final BigDecimal   MIN_VALUE = new BigDecimal(0); // Minimum value of 1 in transactions?
    public static final BigDecimal   MAX_VALUE = new BigDecimal("92233720368.54775807");
    public static final long willetsPerCoin = Coin.COIN.value; // 10^8 (Omni equivalent of Satoshis
    private static final BigDecimal bdWilletsPerCoin = new BigDecimal(willetsPerCoin);

    /**
     * Create OmniDivisibleValue of the specified amount
     * @param amount Number of Omni tokens
     * @return
     */
    public static OmniDivisibleValue of(long amount) {
        return OmniDivisibleValue.of(BigDecimal.valueOf(amount));
    }

    /**
     * Create OmniDivisibleValue of the specified amount
     * @param amount Number of Omni tokens
     * @return
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
    public Class<?> getNumberType() {
        return Long.class;
    }

    @Override
    public int getPrecision() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public int getScale() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public int intValueExact() {
        BigDecimal bd = bigDecimalValue();
        return bd.intValueExact();
    }

    @Override
    public long longValueExact() {
        BigDecimal bd = bigDecimalValue();
        return bd.longValueExact();
    }

    @Override
    public double doubleValueExact() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public <T extends Number> T numberValue(Class<T> numberType) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public NumberValue round(MathContext mathContext) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public <T extends Number> T numberValueExact(Class<T> numberType) {
        throw new UnsupportedOperationException("Operation not supported");
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
        BigDecimal bd = bigDecimalValue();
        return bd.byteValueExact();
    }

    @Override
    public short shortValue() {
        BigDecimal bd = bigDecimalValue();
        return bd.shortValueExact();
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

    @Override
    public String toString() {
        BigDecimal bd = bigDecimalValue();
        return bd.toString();
    }

    public BigDecimal bigDecimalValue() {
        BigDecimal willets = new BigDecimal(value);
        //TODO: Add rounding mode?
        return willets.divide(bdWilletsPerCoin);
    }
}
