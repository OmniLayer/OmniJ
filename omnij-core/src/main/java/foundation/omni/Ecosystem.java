package foundation.omni;

/**
 * Omni Protocol Ecosystem
 */
public enum Ecosystem {
    OMNI((short)1),
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
