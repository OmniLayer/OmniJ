package foundation.omni;

import javax.money.NumberValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Numeric Value of Divisible Omni Token
 */
public class OmniDivisibleValue extends OmniValue {
    public static final BigDecimal   MIN_VALUE = new BigDecimal(0); // Minimum value of 1 in transactions?
    public static final BigDecimal   MAX_VALUE = new BigDecimal("92233720368.54775807");
    public static final long willetsPerCoin = 100000000; // 10^8
    private static final BigInteger biWilletsPerCoin = BigInteger.valueOf(willetsPerCoin);
    private static final BigDecimal bdWilletsPerCoin = new BigDecimal(willetsPerCoin);

    /**
     * Create OmniDivisibleValue from willets/internal/wire format
     *
     * @param willets number of willets
     * @return OmniIndivisibleValue equal to number of willets
     */
    public static OmniDivisibleValue fromWillets(long willets) {
        return new OmniDivisibleValue(willets, true);
    }

    public OmniDivisibleValue(long value) {
        this(BigInteger.valueOf(value));
    }

    public OmniDivisibleValue(BigInteger value) {
        super(value.multiply(biWilletsPerCoin));
    }

    public OmniDivisibleValue(BigDecimal value) {
        super(value.multiply(bdWilletsPerCoin).longValueExact());
    }

    /**
     * This is a hidden mechanism to implement the fromWillets static method.
     *
     * @param number initial value in willets or in coins
     * @param internalFormat true if number is internal/willets/"wire" format
     */
    private OmniDivisibleValue(long number, boolean internalFormat) {
        super(internalFormat ? number : number * willetsPerCoin);
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
        return willets.divide(bdWilletsPerCoin);
    }
}
