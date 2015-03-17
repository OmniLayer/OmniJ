package foundation.omni;

/**
 * Number type to represent an Omni protocol property type.
 */
public class PropertyType extends Number {
    private final int value;

    public static final int INDIVISIBLE_VALUE = 1;
    public static final int DIVISIBLE_VALUE = 2;

    public static final PropertyType INDIVISIBLE = new PropertyType(INDIVISIBLE_VALUE);
    public static final PropertyType DIVISIBLE = new PropertyType(DIVISIBLE_VALUE);

    public PropertyType(int value) {
        if (!(value == INDIVISIBLE_VALUE ||
              value == DIVISIBLE_VALUE)) {
            throw new NumberFormatException();
        }
        this.value = value;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PropertyType)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.value == ((PropertyType)obj).value;
    }

    @Override
    public String toString() {
        return "PropertyType:" + Integer.toString(value);
    }

}
