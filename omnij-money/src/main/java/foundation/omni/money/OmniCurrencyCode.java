package foundation.omni.money;

import foundation.omni.CurrencyID;
import foundation.omni.PropertyType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static foundation.omni.PropertyType.*;

/**
 * CurrencyCode for base and popular OMNI currencies
 * Base is OMNI and TOMNI
 * Others are currencies with market cap over 1M and listed on exchanges
 */
public enum OmniCurrencyCode {
    BTC(CurrencyID.BTC, DIVISIBLE),
    OMNI(CurrencyID.OMNI, DIVISIBLE),
    TOMNI(CurrencyID.TOMNI, DIVISIBLE),
    MAID(CurrencyID.MAID, INDIVISIBLE),
    USDT(CurrencyID.USDT, DIVISIBLE),
    AMP(CurrencyID.AMP, DIVISIBLE),
    SAFEX(CurrencyID.SAFEX, INDIVISIBLE),
    AGRS(CurrencyID.AGRS, DIVISIBLE),
    PDC(CurrencyID.PDC, INDIVISIBLE);

    public static final String realEcosystemPrefix = "OMNI_SPT#";
    public static final String testEcosystemPrefix = "TOMNI_SPT#";
    static final List<OmniCurrencyCode> assignedCodes =
            Arrays.asList(OmniCurrencyCode.class.getEnumConstants());
    private final CurrencyID id;
    private final PropertyType type;

    OmniCurrencyCode(CurrencyID id, PropertyType type) {
        this.id = id;
        this.type = type;
    }

    public CurrencyID id() {
        return id;
    }

    public PropertyType type() {
        return type;
    }

    /**
     * Generate a currency code string from an Omni CurrencyID
     *
     * @param id CurrencyID to convert
     * @return An enumerated code if available, or OMNI_SPT#nnn, or TOMNI_SPT#nnn
     */
    public static String idToCodeString(CurrencyID id) {
        return OmniCurrencyCode
                .idToCode(id)
                .map(Enum::name)
                .orElseGet(() -> synthesizeCodeFromId(id));
    }

    /**
     * Generate a OmniCurrencyCode for an Omni CurrencyID
     *
     * @param id CurrencyID to lookup
     * @return An enumerated code if available, empty otherwise
     */
    public static Optional<OmniCurrencyCode> idToCode(CurrencyID id) {
        final long value = id.getValue();

        return assignedCodes.stream()
                .filter(candidate -> candidate.id.getValue() == value)
                .findAny();
    }

    public static Optional<OmniCurrencyCode> stringToCode(String string) {
        return assignedCodes.stream()
                .filter(candidate -> string.equals(candidate.name()))
                .findAny();
    }

    public static CurrencyID codeToId(String codeString) {
        return stringToCode(codeString)
                .map(code -> code.id)
                .orElse(parseSyntheticCode(codeString).orElse(null));
    }

    private static Optional<CurrencyID> codeStringToId(String codeString) {
        return Optional.empty(); // TBD
    }

    private static Optional<CurrencyID> parseSyntheticCode(String codeString) {
        // TODO: More strict validation of string format
        if (codeString.startsWith(realEcosystemPrefix) || codeString.startsWith(testEcosystemPrefix)) {
            String num = codeString.split("#")[1];
            long val = Long.parseLong(num);
            return Optional.of(new CurrencyID(val));
        }
        return Optional.empty();
    }

    /**
     * Synthesize a currency code string from an Omni Property ID number
     *
     * @param id CurrencyID for non-enumerated currency
     * @return OMNI_SPT#nnn, or TOMNI_SPT#nnn as appropriate
     */
    private static String synthesizeCodeFromId(CurrencyID id) {
        return synthesizeCodeFromId(id.getValue());
    }

    /**
     * Synthesize a currency code string from an Omni Property ID number
     * 
     * @param number Omni property ID for non-enumerated currency
     * @return OMNI_SPT#nnn, or TOMNI_SPT#nnn as appropriate
     */
    private static String synthesizeCodeFromId(long number) {
        return (CurrencyID.isValidReal(number) ? realEcosystemPrefix : testEcosystemPrefix) + number;
    }

}
