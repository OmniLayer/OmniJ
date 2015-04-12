package foundation.omni;

import javax.money.NumberValue;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * <p>Numeric Value of Omni Token - base class for divisible and indivisible subclasses.</p>
 *
 * <p>Known as "Number of Coins" in the Specification</p>
 *
 * <p>Placeholder: Do not use - not ready yet!</p>
 *
 * <p>TODO: Implement (all?) unsupported methods</p>
 * <p>TODO: Should we allow negative values?</p>
 */
public abstract class OmniValue extends NumberValue {
    protected final long value; // internal value format, in willets

    // Willet max/min values, same as max/min for indivisible, but different than for divisible
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = Long.MAX_VALUE; // = 2^63 - 1 = 9223372036854775807L;

    public OmniValue(long value) {
        checkValue(value);
        this.value = value;
    }

    public OmniValue(BigInteger value) {
        checkValue(value);
        this.value = value.longValue();
    }

    /**
     * <p>Make sure a BigInteger value is a valid Omni "number of coins" value</p>
     *
     * @param value
     * @throws NumberFormatException
     */
    public static void checkValue(BigInteger value) throws NumberFormatException {
        if (value.compareTo(BigInteger.valueOf(MIN_VALUE)) == -1) {
            throw new NumberFormatException();
        }
        if (value.compareTo(BigInteger.valueOf(MAX_VALUE)) == 1) {
            throw new NumberFormatException();
        }
    }

    /**
     * <p>Make sure a long value is a valid Omni "number of coins" value</p>
     *
     * <p>Note: Since any positive long is valid, we just need to check that
     * it's not less than MIN_VALUE</p>
     *
     * @param value value to check.
     * @throws NumberFormatException
     */
    public static void checkValue(long value) throws NumberFormatException {
        if (value < MIN_VALUE) {
            throw new NumberFormatException();
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
