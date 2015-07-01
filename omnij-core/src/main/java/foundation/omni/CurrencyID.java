package foundation.omni;

/**
 * Number type to represent a Omni Protocol Currency ID
 */
public final class CurrencyID implements Cloneable {
    private final long value;

    public static final long   MIN_VALUE = 0;
    public static final long   MAX_VALUE = 4294967295L;

    public static final long   MAX_REAL_ECOSYSTEM_VALUE = 2147483647;
    public static final long   MAX_TEST_ECOSYSTEM_VALUE = MAX_VALUE;

    public static final long    BTC_VALUE = 0;
    public static final long    MSC_VALUE = 1;
    public static final long    TMSC_VALUE = 2;
    public static final long    MaidSafeCoin_VALUE = 3;
    public static final long    TetherUS_VALUE = 31;

    public static final CurrencyID  BTC = new CurrencyID(BTC_VALUE);
    public static final CurrencyID  MSC = new CurrencyID(MSC_VALUE);
    public static final CurrencyID  TMSC = new CurrencyID(TMSC_VALUE);
    public static final CurrencyID  MaidSafeCoin = new CurrencyID(MaidSafeCoin_VALUE);
    public static final CurrencyID  TetherUS = new CurrencyID(TetherUS_VALUE);

    public static CurrencyID valueOf(String s) {
        switch (s) {
            case "BTC":
                return BTC;
            case "MSC":
                return MSC;
            case "TMSC":
                return TMSC;
            case "MaidSafeCoin":
                return MaidSafeCoin;
            case "TetherUS":
                return TetherUS;
        }
        throw new NumberFormatException();
    }

    public CurrencyID(long value) {
        if (value < MIN_VALUE) {
            throw new NumberFormatException();
        }
        if (value > MAX_VALUE) {
            throw new NumberFormatException();
        }
        this.value = value;
    }

    public Ecosystem getEcosystem() {
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

    public long longValue() {
        return value;
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
        return "CurrencyID:" + Long.toString(value);
    }

}
