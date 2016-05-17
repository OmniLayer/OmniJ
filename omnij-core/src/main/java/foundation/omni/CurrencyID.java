package foundation.omni;

/**
 * Number type to represent a Omni Protocol Currency ID
 */
public final class CurrencyID implements Cloneable, Comparable<CurrencyID> {
    private final long value;

    public static final long   MIN_VALUE = 0;
    public static final long   MAX_VALUE = 4294967295L;

    public static final long   MAX_REAL_ECOSYSTEM_VALUE = 2147483647;
    public static final long   MAX_TEST_ECOSYSTEM_VALUE = MAX_VALUE;

    public static final long    BTC_VALUE = 0;
    public static final long    OMNI_VALUE = 1;
    public static final long    TOMNI_VALUE = 2;
    public static final long    MAID_VALUE = 3; // MaidSafeCoin
    /** @deprecated */
    public static final long    MaidSafeCoin_VALUE = 3;
    /** @deprecated */
    public static final long    TetherUS_VALUE = 31;
    public static final long    USDT_VALUE = 31;
    public static final long    AMP_VALUE = 39; // Synerio
    public static final long    EURT_VALUE = 41;
    public static final long    SEC_VALUE = 56; // SafeExchangeCoin
    public static final long    AGRS_VALUE = 58; // Agoras

    public static final CurrencyID  BTC = new CurrencyID(BTC_VALUE);
    public static final CurrencyID  OMNI = new CurrencyID(OMNI_VALUE);
    public static final CurrencyID  TOMNI = new CurrencyID(TOMNI_VALUE);
    public static final CurrencyID  MAID = new CurrencyID(MAID_VALUE);
    /** @deprecated */
    public static final CurrencyID  MaidSafeCoin = new CurrencyID(MAID_VALUE);
    /** @deprecated */
    public static final CurrencyID  TetherUS = new CurrencyID(USDT_VALUE);
    public static final CurrencyID  USDT = new CurrencyID(USDT_VALUE);
    public static final CurrencyID  AMP = new CurrencyID(AMP_VALUE);
    public static final CurrencyID  EURT = new CurrencyID(EURT_VALUE);
    public static final CurrencyID  SEC = new CurrencyID(SEC_VALUE);
    public static final CurrencyID  AGRS = new CurrencyID(AGRS_VALUE);

    public static CurrencyID valueOf(String s) {
        switch (s) {
            case "BTC":
                return BTC;
            case "OMNI":
                return OMNI;
            case "TOMNI":
                return TOMNI;
            case "MAID":
                return MAID;
            case "USDT":
                return USDT;
            case "AMP":
                return AMP;
            case "EURT":
                return EURT;
            case "SEC":
                return SEC;
            case "AGRS":
                return AGRS;
        }
        throw new IllegalArgumentException("unknown currency ticker string");
    }

    public CurrencyID(long value) {
        if (value < MIN_VALUE) {
            throw new IllegalArgumentException("below min");
        }
        if (value > MAX_VALUE) {
            throw new IllegalArgumentException("above max");
        }
        this.value = value;
    }

    public Ecosystem getEcosystem() {
        if (value == OMNI_VALUE) {
            return Ecosystem.MSC;
        } else if (value == TOMNI_VALUE) {
            return Ecosystem.TMSC;
        } else if (value <= MAX_REAL_ECOSYSTEM_VALUE) {
            return Ecosystem.MSC;
        } else {
            return Ecosystem.TMSC;
        }
    }

    /**
     * is valid BTC or Real ecosystem or Test ecosystem
     * @param value currency id number
     * @return true if valid
     */
    public static boolean isValid(long value) {
        return ((value >= MIN_VALUE) && (value <= MAX_VALUE));
    }

    /**
     * Is valid real ecosystem value
     * Note BTC is not a valid real ecosystem value
     * @param value currency id number
     * @return true if valid
     */
    public static boolean isValidReal(long value) {
        return (value == OMNI_VALUE) || ((value > TOMNI_VALUE) && (value <= MAX_REAL_ECOSYSTEM_VALUE));
    }

    /**
     * Is valid test ecosystem value
     * @param value currency id number
     * @return true if valid
     */
    public static boolean isValidTest(long value) {
        return ((value == TOMNI_VALUE) ||(value > MAX_REAL_ECOSYSTEM_VALUE) && (value <= MAX_VALUE));
    }

    public long getValue() {
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

    @Override
    public int compareTo(CurrencyID o) {
        return Long.compare(this.value, o.value);
    }
}
