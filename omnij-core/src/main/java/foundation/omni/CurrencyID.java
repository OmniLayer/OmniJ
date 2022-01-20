package foundation.omni;

/**
 * Omni Protocol Currency Identifier
 * <p>
 * According to the OmniLayer Specification, Currency Identifier is an unsigned 32-bit value.
 * For storage efficiency reasons we are using a Java primitive (signed) {@code int} for storing
 * the value. To implement this correctly we internally use {@link Integer#toUnsignedLong(int)} when
 * converting to a (signed) {@code long}. When converting from a valid currency id in a (signed) {@code long} simply
 * using a cast operator produces the correct {@code int} result.
 * <p>
 * All public interfaces use {@code long} except {@link CurrencyID#ofUnsigned(int)} and {@link CurrencyID#unsignedIntValue()}
 * which should be used with caution. Internal conversion to {@code long} uses {@link CurrencyID#value()}.
 */
public final class CurrencyID implements Comparable<CurrencyID> {
    private final int value;

    public static final long   MIN_VALUE = 0;
    public static final long   MAX_VALUE = 4_294_967_295L;

    public static final long   MAX_REAL_ECOSYSTEM_VALUE = 2_147_483_647;
    public static final long   MAX_TEST_ECOSYSTEM_VALUE = MAX_VALUE;

    public static final long    BTC_VALUE = 0;
    public static final long    OMNI_VALUE = 1;
    public static final long    TOMNI_VALUE = 2;
    public static final long    MAID_VALUE = 3; // MaidSafeCoin
    public static final long    USDT_VALUE = 31;
    public static final long    AMP_VALUE = 39; // Synerio
    public static final long    EURT_VALUE = 41;
    public static final long    SAFEX_VALUE = 56; // SafeExchangeCoin
    public static final long    AGRS_VALUE = 58; // Agoras
    public static final long    PDC_VALUE = 59; // Project Decorum Coin

    public static final CurrencyID  BTC = new CurrencyID(BTC_VALUE);
    public static final CurrencyID  OMNI = new CurrencyID(OMNI_VALUE);
    public static final CurrencyID  TOMNI = new CurrencyID(TOMNI_VALUE);
    public static final CurrencyID  MAID = new CurrencyID(MAID_VALUE);
    public static final CurrencyID  USDT = new CurrencyID(USDT_VALUE);
    public static final CurrencyID  AMP = new CurrencyID(AMP_VALUE);
    public static final CurrencyID  EURT = new CurrencyID(EURT_VALUE);
    public static final CurrencyID  SAFEX = new CurrencyID(SAFEX_VALUE);
    public static final CurrencyID  AGRS = new CurrencyID(AGRS_VALUE);
    public static final CurrencyID  PDC = new CurrencyID(PDC_VALUE);

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
            case "SAFEX":
                return SAFEX;
            case "AGRS":
                return AGRS;
        }
        throw new IllegalArgumentException("unknown currency ticker string");
    }

    public static CurrencyID of(long idValue) {
        return new CurrencyID(idValue);
    }

    public static CurrencyID ofUnsigned(int unsignedIntValue) {
        return new CurrencyID(unsignedIntValue);
    }

    /**
     * Construct from a valid {@code long} value.
     *
     * @param value An valid currency id value (32-bit unsigned)
     * @throws IllegalArgumentException if value is out-out-range
     */
    public CurrencyID(long value) {
        this(validateAndConvert(value));
    }

    /**
     * Private constructor that takes an unsigned {@code int} value.
     *
     * @param value An unsigned int representing a currency id value
     */
    private CurrencyID(int value) {
        this.value = value;
    }

    public Ecosystem getEcosystem() {
        if (value == OMNI_VALUE) {
            return Ecosystem.OMNI;
        } else if (value == TOMNI_VALUE) {
            return Ecosystem.TOMNI;
        } else if (value() <= MAX_REAL_ECOSYSTEM_VALUE) {
            return Ecosystem.OMNI;
        } else {
            return Ecosystem.TOMNI;
        }
    }

    /**
     * is valid BTC or Real ecosystem or Test ecosystem
     * @param value currency id number
     * @return true if valid
     */
    public static boolean isValid(long value) {
        return (value >= MIN_VALUE) && (value <= MAX_VALUE);
    }

    private static int validateAndConvert(long value) throws IllegalArgumentException {
        if (value < MIN_VALUE) {
            throw new IllegalArgumentException("below min");
        }
        if (value > MAX_VALUE) {
            throw new IllegalArgumentException("above max");
        }
        return (int) value;
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
        return (value == TOMNI_VALUE) || (value > MAX_REAL_ECOSYSTEM_VALUE) && (value <= MAX_VALUE);
    }

    /**
     * Return the 32-bit unsigned value in a Java {@code long}.
     * <p>
     * Uses {@link Integer#toUnsignedLong(int)} to do a bitwise <i>and</i> operation
     * to prevent sign extension.
     *
     * @return CurrencyID value as a {@code long}
     */
    public long getValue() {
        return value();
    }

    public long value() {
        return Integer.toUnsignedLong(value);
    }

    /**
     * Return the 32-bit unsigned value in a Java {@code int}.
     * <p>
     * Use this method with caution as Java treats {@code int}s as signed.
     *
     * @return CurrencyID value as a {@code int}
     */
    public int unsignedIntValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(value).hashCode();
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
        return "CurrencyID:" + value();
    }

    @Override
    public int compareTo(CurrencyID o) {
        return Long.compare(this.value(), o.value());
    }
}
