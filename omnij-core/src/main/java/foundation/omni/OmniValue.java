package foundation.omni;

import javax.money.NumberValue;
import java.math.MathContext;

/**
 * Omni "Number of Coins" (tokens) value
 *
 * Placeholder: Do not use - not ready yet!
 */
public class OmniValue extends NumberValue {
    public static final long   MIN_VALUE = 0; // Minimum value of 1 in transactions?
    public static final long   MAX_VALUE = 9223372036854775807L;

    private final long value;

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
        return 0;
    }

    @Override
    public int getScale() {
        return 0;
    }

    @Override
    public int intValueExact() {
        return 0;
    }

    @Override
    public long longValueExact() {
        return value;
    }

    @Override
    public double doubleValueExact() {
        return 0;
    }

    @Override
    public <T extends Number> T numberValue(Class<T> numberType) {
        return null;
    }

    @Override
    public NumberValue round(MathContext mathContext) {
        return null;
    }

    @Override
    public <T extends Number> T numberValueExact(Class<T> numberType) {
        return null;
    }

    @Override
    public long getAmountFractionNumerator() {
        return 0;
    }

    @Override
    public long getAmountFractionDenominator() {
        return 0;
    }

    @Override
    public int intValue() {
        return 0;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return 0;
    }
}
