package foundation.omni;

import javax.money.NumberValue;
import java.math.MathContext;

/**
 * Omni "Number of Coins" (tokens) value
 *
 * Placeholder: Do not use - not ready yet!
 *
 * TODO: Implement all the unsupported methods
 * TODO: Implement divisible and indivisible (as subclasses?)
 */
public class OmniValue extends NumberValue {
    private final long value;

    // TODO: Should we allow negative values?
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = 9223372036854775807L;


    public OmniValue(long value) {
        if (value < MIN_VALUE) {
            throw new NumberFormatException();
        }
        if (value > MAX_VALUE) {
            throw new NumberFormatException();
        }
        this.value = value;
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
