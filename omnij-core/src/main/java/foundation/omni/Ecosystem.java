package foundation.omni;

/**
 * Omni Protocol Ecosystem
 */
public enum Ecosystem {
    /** The "real" or "production" Omni ecosystem */
    OMNI((short)1),
    /** The test Omni ecosystem */
    TOMNI((short)2);

    private final short value;

    Ecosystem(short s) {
        value = s;
    }

    /**
     * @return numeric value of Ecosystem
     */
    public short value() {
        return value;
    }

    /**
     *
     * @param s numeric value for Ecosystem
     * @return the corresponding {@code enum}
     */
    public static Ecosystem valueOf(short s) {
        return s == OMNI.value() ? OMNI : TOMNI;
    }

    /**
     * @return the value
     * @deprecated Use {@link Ecosystem#value()}
     */
    @Deprecated
    public short getValue() {
        return value;
    }
}
