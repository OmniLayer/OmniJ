package org.mastercoin;

/**
 * Number type to represent a Master Protocol Currency ID
 */
public final class CurrencyID extends Number implements Cloneable {
    private final long value;

    public static final long   MIN_VALUE = 1;
    public static final long   MAX_VALUE = 4294967295L;

    public static final long   MAX_REAL_ECOSYSTEM_VALUE = 2147483647;
    public static final long   MAX_TEST_ECOSYSTEM_VALUE = MAX_VALUE;

    public static final long    MSC_VALUE = 1;
    public static final long    TMSC_VALUE = 2;
    public static final long    MaidSafeCoin_VALUE = 3;
    public static final long    TetherUS_VALUE = 31;

    public static final CurrencyID  MSC = new CurrencyID(MSC_VALUE);
    public static final CurrencyID  TMSC = new CurrencyID(TMSC_VALUE);
    public static final CurrencyID  MaidSafeCoin = new CurrencyID(MaidSafeCoin_VALUE);
    public static final CurrencyID  TetherUS = new CurrencyID(TetherUS_VALUE);

    public CurrencyID(Long value) {
        if (value < MIN_VALUE) {
            throw new NumberFormatException();
        }
        if (value > MAX_VALUE) {
            throw new NumberFormatException();
        }
        this.value = value;
    }

    public Ecosystem ecosystem() {
        if (value == MSC_VALUE) {
            return Ecosystem.MSC;
        } else if (value == TMSC_VALUE) {
            return Ecosystem.TMSC;
        } else if (value <= MAX_REAL_ECOSYSTEM_VALUE) {
            return Ecosystem.MSC;
        } else {
            return Ecosystem.TMSC;
        }
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
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
    public int hashCode() {
        return Long.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CurrencyID)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.value == ((CurrencyID)obj).value;
    }

    @Override
    public String toString() {
        return "CurrencyID:" + Integer.toString(this.intValue());
    }

}
