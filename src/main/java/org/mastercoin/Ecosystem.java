package org.mastercoin;

/**
 * User: sean
 * Date: 7/12/14
 * Time: 4:44 PM
 */
public class Ecosystem extends Number {
    private final short value;

    public static final short   MIN_VALUE = 1;
    public static final short   MAX_VALUE = 2;

    public static final short   MSC_VALUE = 1;
    public static final short   TMSC_VALUE = 2;

    public static final Ecosystem   MSC = new Ecosystem(MSC_VALUE);
    public static final Ecosystem   TMSC = new Ecosystem(TMSC_VALUE);

    public Ecosystem(short value) {
        if (value < MIN_VALUE) {
            throw new NumberFormatException();
        }
        if (value > MAX_VALUE) {
            throw new NumberFormatException();
        }
        this.value = value;
    }

    public Ecosystem(int value) {
        this((short) value);
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public double doubleValue() {
        return (double) value;
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
}
