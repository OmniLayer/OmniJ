package foundation.omni;

import org.bitcoinj.core.Coin;

import javax.money.NumberValue;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * <p>Numeric value for a quantity of Omni tokens - base class for OmniDivisible and OmniIndivisible subclasses.
 * Known as "Number of Coins" in the Omni Protocol Specification.</p>
 *
 * <p>The internal representation is a <code>long</code> which corresponds to what we call a
 * "willett" in honour of J.R. Willett in the same fashion as the smallest Bitcoin unit is called a "satoshi".</p>
 *
 * <p>The constructors are <code>protected</code> and instances should be created with the <code>of()</code>
 * static methods which can take either <code>BigDecimal</code> or <code>long</code> values as parameters.</p>
 *
 * <p>The various *<code>value()</code> methods of <code>Number</code> and <code>NumberValue</code> will
 * return values as used in the Omni Protocol Specification, which means that <b>for divisible tokens the
 * values will be treated a decimal values and methods returning integer types will be throw exceptions if their
 * is a fractional component that would be truncated.</b></p>
 *
 * TODO: Consider refactoring to extend java.lang.Number to remove dependency on javax.money in omnij-core
 *
 * <p>TODO: provide examples of *value() methods and what they return</p>
 *
 * <p>TODO: Implement unsupported methods</p>
 * <p>TODO: Should we allow negative values?</p>
 */
public abstract class OmniValue extends NumberValue {
    public static final long willettsPerDivisible = Coin.COIN.value; // 10^8 (Omni equivalent of satoshi unit
    protected final long willetts; // internal value format, in willetts

    // Willett max/min values, same as max/min for indivisible, but different than for divisible
    public static final long MIN_WILLETTS = 0; // Minimum value of 1 in transactions?
    public static final long MAX_WILLETTS = Long.MAX_VALUE; // = 2^63 - 1 = 9_223_372_036_854_775_807L;

    // -9_223_372_036_854_775_616

    /**
     * Default Constructor
     * Used only by subclasses using internal (willetts) format
     *
     * @param willetts Willetts (internal/wire format)
     */
    protected OmniValue(long willetts) {
        checkWillettValue(willetts);
        this.willetts = willetts;
    }

    public static OmniValue of(BigDecimal amount, PropertyType type) {
        return of(amount, type.equals(PropertyType.DIVISIBLE));
    }

    public static OmniValue of(long amount, PropertyType type) {
        return of(amount, type.equals(PropertyType.DIVISIBLE));
    }

    public static OmniValue of(BigDecimal amount, boolean divisible) {
        return divisible ? OmniDivisibleValue.of(amount) : OmniIndivisibleValue.of(amount.longValueExact());
    }

    public static OmniValue of(long amount, boolean divisible) {
        return divisible ? OmniDivisibleValue.of(amount) : OmniIndivisibleValue.of(amount);
    }

    public static OmniValue ofWilletts(long amount, PropertyType type) {
        return ofWilletts(amount, type.equals(PropertyType.DIVISIBLE));
    }

    public static OmniValue ofWilletts(long amount, boolean divisible) {
        return divisible ? OmniDivisibleValue.ofWilletts(amount) : OmniIndivisibleValue.ofWilletts(amount);
    }

    public long getWilletts() {
        return willetts;
    }

    abstract public PropertyType getPropertyType();

    /**
     * <p>Make sure a BigInteger value is a valid Omni "number of coins" (willetts) value</p>
     *
     * @param willetts "number of coins" (willetts) value to check
     * @throws ArithmeticException if less than minimum or greater than maximum allowed value
     */
    public static void checkWillettValue(BigInteger willetts) throws ArithmeticException {
        if (willetts.compareTo(BigInteger.valueOf(MIN_WILLETTS)) < 0) {
            throw new ArithmeticException();
        }
        if (willetts.compareTo(BigInteger.valueOf(MAX_WILLETTS)) > 0) {
            throw new ArithmeticException();
        }
    }

    /**
     * <p>Make sure a long value is a valid Omni "number of coins" value</p>
     *
     * <p>Note: Since any positive long is valid, we just need to check that
     * it's not less than MIN_VALUE</p>
     *
     * @param willetts value to check.
     * @throws ArithmeticException if less than minimum allowed value
     */
    public static void checkWillettValue(long willetts) throws ArithmeticException {
        if (willetts < MIN_WILLETTS) {
            throw new ArithmeticException();
        }
    }

    @Override
    abstract public Class<? extends Number> getNumberType();

    /**
     * Return value in preferred number type
     * @return value as represented in best/preferred number type
     */
    abstract public Number numberValue();

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
        if (willetts > Integer.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to integer");
        }
        return (int) willetts;
    }

    @Override
    public long longValueExact() {
        return willetts;
    }

    @Override
    abstract public double doubleValueExact();

    @Override
    public <T extends Number> T numberValue(Class<T> numberType) {
        return ConvertNumberValue.of(numberType, numberValue());
    }

    @Override
    public <T extends Number> T numberValueExact(Class<T> numberType) {
        return ConvertNumberValue.ofExact(numberType, numberValue());
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
        if (willetts > Byte.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to byte");
        }
        return (byte) willetts;
    }

    @Override
    public short shortValue() {
        if (willetts > Short.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to short");
        }
        return (short) willetts;
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

    /**
     * Return a double value. Warning: this will result in rounding errors.
     * Only use this for applications like plotting data, never for anything
     * that counts currency.

     * @return The value rounded to the nearest {@code double}.
     */
    @Override
    abstract public double doubleValue();

    @Override
    public int hashCode() {
        return Long.hashCode(willetts);
    }

    @Override
    abstract public boolean equals(Object obj);

    @Override
    public int compareTo(NumberValue o) {
        if (o instanceof OmniValue) {
            return Long.compare(this.willetts, ((OmniValue) o).willetts);
        } else {
            return Long.compare(this.willetts, o.longValueExact());
        }
    }

    /**
     * Convert to {@link String}. Uses standard JVM {@code toString()} formats. For {@link OmniDivisibleValue} it is the same format as
     * {@link BigDecimal} (this means{@code "0E-8"} for {@link OmniDivisibleValue#ZERO}.) For {@link OmniIndivisibleValue}
     * it is the same format as {@link Long}.
     * @return Number as a string
     */
    @Override
    abstract public String toString();

    /**
     * Convert to {@link String}. For {@link OmniDivisibleValue} the same format as {@link BigDecimal#toPlainString()}. For
     * {@link OmniIndivisibleValue} it is the same as {@link OmniIndivisibleValue#toString()}.
     * @return Number as a string
     */
    abstract public String toPlainString();

    /**
     * Convert to {@link String}. The recommended format for OmniValues in JSON. For {@link OmniDivisibleValue} there is always at least one
     * place after the decimal point (e.g. {@code "0.0"}). This allows humans and parsers to easily tell {@link OmniDivisibleValue}s
     * from {@link OmniIndivisibleValue}s.
     * @return Number as a string
     */
    abstract public String toJsonFormattedString();

    /**
     * Convert to {@link String}. A more human-readable format using comma place-separators.
     * @return Number as a string
     */
    abstract public String toFormattedString();

    abstract public BigDecimal bigDecimalValue();

    public OmniValue plus(OmniValue right) {
        if (this instanceof OmniDivisibleValue && right instanceof OmniDivisibleValue) {
            return OmniDivisibleValue.ofWilletts(this.willetts + right.willetts);
        } else if (this instanceof OmniIndivisibleValue && right instanceof OmniIndivisibleValue) {
            return OmniIndivisibleValue.ofWilletts(this.willetts + right.willetts);
        } else {
            throw new ArithmeticException("Can't use plus with mixed OmniDivisible and OmniIndivisible operands");
        }
    }

    public OmniValue minus(OmniValue right) {
        if (this instanceof OmniDivisibleValue && right instanceof OmniDivisibleValue) {
            return OmniDivisibleValue.of(this.willetts - right.willetts);
        } else if (this instanceof OmniIndivisibleValue && right instanceof OmniIndivisibleValue) {
            return OmniIndivisibleValue.of(this.willetts - right.willetts);
        } else {
            throw new ArithmeticException("Can't use minus with mixed OmniDivisible and OmniIndivisible operands");
        }
    }

    public OmniValue multiply(long right) {
        if (this instanceof OmniDivisibleValue) {
            return OmniDivisibleValue.of(this.willetts * right);
        } else if (this instanceof OmniIndivisibleValue) {
            return OmniIndivisibleValue.of(this.willetts * right);
        } else {
            throw new ArithmeticException("Can't use multiply with class other than OmniDivisible or OmniIndivisible");
        }
    }

}
