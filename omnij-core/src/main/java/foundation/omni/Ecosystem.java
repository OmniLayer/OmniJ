package foundation.omni;

/**
 * Type to represent Omni Protocol Ecosystem
 *
 * TODO: Should Ecosystem be made an enum?
 */
public class Ecosystem {
    private final short value;

    public static final short   MIN_VALUE = 1;
    public static final short   MAX_VALUE = 2;

    public static final short   OMNI_VALUE = 1;
    /** @deprecated */
    public static final short   MSC_VALUE = 1;
    public static final short   TOMNI_VALUE = 2;
    /** @deprecated */
    public static final short   TMSC_VALUE = 2;

    public static final Ecosystem OMNI = new Ecosystem(OMNI_VALUE);
    /** @deprecated */
    public static final Ecosystem MSC = new Ecosystem(OMNI_VALUE);
    public static final Ecosystem TOMNI = new Ecosystem(TOMNI_VALUE);
    /** @deprecated */
    public static final Ecosystem TMSC = new Ecosystem(TOMNI_VALUE);

    private Ecosystem(short value) {
        if (value < MIN_VALUE) {
            throw new NumberFormatException();
        }
        if (value > MAX_VALUE) {
            throw new NumberFormatException();
        }
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Short.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Ecosystem)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.value == ((Ecosystem)obj).value;
    }

    @Override
    public String toString() {
        return "Ecosystem:" + Short.toString(value);
    }

}
