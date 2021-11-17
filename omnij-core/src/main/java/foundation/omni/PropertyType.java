package foundation.omni;

/**
 * Omni Protocol Property Type.
 */
public enum PropertyType {
    INDIVISIBLE(1),
    DIVISIBLE(2);

    private final int value;

    /**
     * @deprecated Use {@link PropertyType#value()}
     */
    @Deprecated
    public static final int INDIVISIBLE_VALUE = 1;
    /**
     * @deprecated Use {@link PropertyType#value()}
     */
    @Deprecated
    public static final int DIVISIBLE_VALUE = 2;

    /**
     * PropertyType from `isDivisible` boolean
     *
     * @param isDivisible true if divisible, false if indivisible
     * @return corresponding PropertyType
     */
    public static PropertyType of(boolean isDivisible) {
        return isDivisible ? PropertyType.DIVISIBLE : PropertyType.INDIVISIBLE;
    }

    PropertyType(int value) {
        this.value = value;
    }

    /**
     * @return PropertyType int value
     */
    public int value() {
        return value;
    }

    public static PropertyType valueOf(int i) {
        return i == INDIVISIBLE.value() ? INDIVISIBLE : DIVISIBLE;
    }

    /**
     * @return true if {@code DIVISIBLE}
     */
    public boolean divisible() {
        return value == DIVISIBLE.value();
    }
    
    /**
     * @return PropertyType int value
     * @deprecated Use {@link PropertyType#value()}
     */
    @Deprecated
    public int getValue() {
        return value;
    }

    /**
     * @return true if {@code DIVISIBLE}
     * @deprecated Use {@link PropertyType#divisible()}
     */
    @Deprecated
    public boolean isDivisible() {
        return value == DIVISIBLE.value();
    }
}
