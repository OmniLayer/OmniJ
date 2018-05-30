package foundation.omni;

/**
 * Omni Protocol Property Type.
 *
 * TODO: Should PropertyType be made an enum?
 */
public class PropertyType {
    private final int value;

    public static final int INDIVISIBLE_VALUE = 1;
    public static final int DIVISIBLE_VALUE = 2;

    public static final PropertyType INDIVISIBLE = new PropertyType(INDIVISIBLE_VALUE);
    public static final PropertyType DIVISIBLE = new PropertyType(DIVISIBLE_VALUE);

    /**
     * PropertyType from `isDivisible` boolean
     *
     * @param isDivisible true if divisible, false if indivisible
     * @return corresponding PropertyType
     */
    public static PropertyType of(boolean isDivisible) {
        return isDivisible ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
    }

    private PropertyType(int value) {
        if (!(value == INDIVISIBLE_VALUE ||
              value == DIVISIBLE_VALUE)) {
            throw new NumberFormatException();
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isDivisible() {
        return value == DIVISIBLE_VALUE;
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
