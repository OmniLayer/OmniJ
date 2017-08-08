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
 * "willet" in honour of J.R. Willet in the same fashion as the smallest Bitcoin unit is called a "satoshi".</p>
 *
 * <p>The constructors are <code>protected</code> and instances should be created with the <code>of()</code>
 * static methods which can take either <code>BigDecimal</code> or <code>long</code> values as parameters.</p>
 *
 * <p>The various *<code>value()</code> methods of <code>Number</code> and <code>NumberValue</code> will
 * return values as used in the Omni Protocol Specification, which means that <b>for divisible tokens the
 * values will be treated a decimal values and methods returning integer types will be throw exceptions if their
 * is a fractional component that would be truncated.</b></p>
 *
 * <p>TODO: provide examples of *value() methods and what they return</p>
 *
 * <p>TODO: Implement unsupported methods</p>
 * <p>TODO: Should we allow negative values?</p>
 */
public abstract class OmniValue extends NumberValue {
    public static final long willetsPerDivisible = Coin.COIN.value; // 10^8 (Omni equivalent of satoshi unit
    protected final long willets; // internal value format, in willets

    // Willet max/min values, same as max/min for indivisible, but different than for divisible
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = Long.MAX_VALUE; // = 2^63 - 1 = 9223372036854775807L;

    /**
     * Default Constructor
     * Used only by subclasses using internal (willets) format
     *
     * @param willets Willets (internal/wire format)
     */
    protected OmniValue(long willets) {
        checkValue(willets);
        this.willets = willets;
    }

    public static OmniValue of(BigDecimal amount, PropertyType type) {
        return type.equals(PropertyType.DIVISIBLE) ?
                OmniDivisibleValue.of(amount) : OmniIndivisibleValue.of(amount.longValueExact());
    }

    public static OmniValue of(long amount, PropertyType type) {
        return type.equals(PropertyType.DIVISIBLE) ?
                OmniDivisibleValue.of(amount) : OmniIndivisibleValue.of(amount);
    }

    public static OmniValue ofWillets(long amount, PropertyType type) {
        return type.equals(PropertyType.DIVISIBLE) ?
                OmniDivisibleValue.ofWillets(amount) : OmniIndivisibleValue.ofWillets(amount);
    }

    public long getWillets() {
        return willets;
    }

    abstract public PropertyType getPropertyType();

    /**
     * <p>Make sure a BigInteger value is a valid Omni "number of coins" (willets) value</p>
     *
     * @param willets "number of coins" (willets) value to check
     * @throws ArithmeticException if less than minimum or greater than maximum allowed value
     */
    public static void checkValue(BigInteger willets) throws ArithmeticException {
        if (willets.compareTo(BigInteger.valueOf(MIN_VALUE)) < 0) {
            throw new ArithmeticException();
        }
        if (willets.compareTo(BigInteger.valueOf(MAX_VALUE)) > 0) {
            throw new ArithmeticException();
        }
    }

    /**
     * <p>Make sure a long value is a valid Omni "number of coins" value</p>
     *
     * <p>Note: Since any positive long is valid, we just need to check that
     * it's not less than MIN_VALUE</p>
     *
     * @param willets value to check.
     * @throws ArithmeticException if less than minimum allowed value
     */
    public static void checkValue(long willets) throws ArithmeticException {
        if (willets < MIN_VALUE) {
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
        if (willets > Integer.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to integer");
        }
        return (int) willets;
    }

    @Override
    public long longValueExact() {
        return willets;
    }

    @Override
    public double doubleValueExact() {
        throw new UnsupportedOperationException("Operation not supported");
    }

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
        if (willets > Byte.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to byte");
        }
        return (byte) willets;
    }

    @Override
    public short shortValue() {
        if (willets > Short.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to short");
        }
        return (short) willets;
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
    public int hashCode() {
        return Long.valueOf(willets).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OmniValue)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.willets == ((OmniValue)obj).willets;
    }

    @Override
    public int compareTo(NumberValue o) {
        if (o instanceof OmniValue) {
            return Long.compare(this.willets, ((OmniValue) o).willets);
        } else {
            return Long.compare(this.willets, o.longValueExact());
        }
    }

    @Override
    public String toString() {
        return numberValue().toString();
    }

    public  abstract BigDecimal bigDecimalValue();

    public OmniValue plus(OmniValue right) {
        if (this instanceof OmniDivisibleValue && right instanceof OmniDivisibleValue) {
            return OmniDivisibleValue.ofWillets(this.willets + right.willets);
        } else if (this instanceof OmniIndivisibleValue && right instanceof OmniIndivisibleValue) {
            return OmniIndivisibleValue.ofWillets(this.willets + right.willets);
        } else {
            throw new ArithmeticException("Can't use plus with mixed OmniDivisible and OmniIndivisible operands");
        }
    }

    public OmniValue minus(OmniValue right) {
        if (this instanceof OmniDivisibleValue && right instanceof OmniDivisibleValue) {
            return OmniDivisibleValue.of(this.willets - right.willets);
        } else if (this instanceof OmniIndivisibleValue && right instanceof OmniIndivisibleValue) {
            return OmniIndivisibleValue.of(this.willets - right.willets);
        } else {
            throw new ArithmeticException("Can't use minus with mixed OmniDivisible and OmniIndivisible operands");
        }
    }

    public OmniValue multiply(long right) {
        if (this instanceof OmniDivisibleValue) {
            return OmniDivisibleValue.of(this.willets * right);
        } else if (this instanceof OmniIndivisibleValue) {
            return OmniIndivisibleValue.of(this.willets * right);
        } else {
            throw new ArithmeticException("Can't use multiply with class other than OmniDivisible or OmniIndivisible");
        }
    }

}
