package foundation.omni;

import javax.money.NumberValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.PropertyPermission;

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
 * return values as used in the Omni Protocol Specification, which means that <bold>for divisible tokens the
 * values will be treated a decimal values and methods returning integer types will be throw exceptions if their
 * is a fractional component that would be truncated.</bold></p>
 *
 * <p>TODO: provide examples of *value() methods and what they return</p>
 *
 * <p>TODO: Implement unsupported methods</p>
 * <p>TODO: Should we allow negative values?</p>
 */
public abstract class OmniValue extends NumberValue {
    protected final long value; // internal value format, in willets

    // Willet max/min values, same as max/min for indivisible, but different than for divisible
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = Long.MAX_VALUE; // = 2^63 - 1 = 9223372036854775807L;

    /**
     * Default Constructor
     * Used only by subclasses using internal (willets) format
     *
     * @param value Willets (internal/wire format)
     */
    protected OmniValue(long value) {
        checkValue(value);
        this.value = value;
    }

    @Deprecated
    protected OmniValue(BigInteger value) {
        checkValue(value);
        this.value = value.longValue();
    }

    public static OmniValue of(BigDecimal amount, PropertyType type) {
        return type.equals(PropertyType.DIVISIBLE) ?
                OmniDivisibleValue.of(amount) : OmniIndivisibleValue.of(amount.longValueExact());
    }

    public static OmniValue of(long amount, PropertyType type) {
        return type.equals(PropertyType.DIVISIBLE) ?
                OmniDivisibleValue.of(amount) : OmniIndivisibleValue.of(amount);
    }

    public long asWillets() {
        return value;
    }

    abstract public PropertyType getPropertyType();

    /**
     * <p>Make sure a BigInteger value is a valid Omni "number of coins" value</p>
     *
     * @param value
     * @throws ArithmeticException
     */
    public static void checkValue(BigInteger value) throws ArithmeticException {
        if (value.compareTo(BigInteger.valueOf(MIN_VALUE)) == -1) {
            throw new ArithmeticException();
        }
        if (value.compareTo(BigInteger.valueOf(MAX_VALUE)) == 1) {
            throw new ArithmeticException();
        }
    }

    /**
     * <p>Make sure a long value is a valid Omni "number of coins" value</p>
     *
     * <p>Note: Since any positive long is valid, we just need to check that
     * it's not less than MIN_VALUE</p>
     *
     * @param value value to check.
     * @throws ArithmeticException
     */
    public static void checkValue(long value) throws ArithmeticException {
        if (value < MIN_VALUE) {
            throw new ArithmeticException();
        }
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
        if (value > Integer.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to integer");
        }
        return (int) value;
    }

    @Override
    public long longValueExact() {
        return value;
    }

    @Override
    public double doubleValueExact() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public <T extends Number> T numberValue(Class<T> numberType) {
        throw new UnsupportedOperationException("Operation not supported");
//        switch (numberType) {
//            case (Class<T>) Long.class:
//                return (T) new Long(0);
//                break;
//            default:
//                throw new UnsupportedOperationException("unsupported type");
//        }
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
        if (value > Byte.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to byte");
        }
        return (byte) value;
    }

    @Override
    public short shortValue() {
        if (value > Short.MAX_VALUE) {
            throw new ArithmeticException("Value too big to be converted to short");
        }
        return (short) value;
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
        return Long.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OmniValue)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.value == ((OmniValue)obj).value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

}
