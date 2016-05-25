package foundation.omni.money;

import foundation.omni.CurrencyID;
import foundation.omni.PropertyType;

import java.util.Arrays;
import java.util.List;

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
    SEC(CurrencyID.SEC, INDIVISIBLE),
    AGRS(CurrencyID.AGRS, DIVISIBLE);

    public static String realEcosystemPrefix = "OMNI_SPT#";
    public static String testEcosystemPrefix = "TOMNI_SPT#";
    public static List<OmniCurrencyCode> assignedCodes =
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

    public static String idToCodeString(CurrencyID id) {
        final long value = id.getValue();

        for (OmniCurrencyCode candidate : assignedCodes) {
            if (value == candidate.id.getValue()) {
                return candidate.name();
            }
        }
        if (CurrencyID.isValidReal(value)) {
            return realEcosystemPrefix + value;
        }
        return testEcosystemPrefix + value;
    }

    public static CurrencyID codeToId(String code) {
        for (OmniCurrencyCode candidate : assignedCodes) {
            if (code.equals(candidate.name())) {
                return candidate.id;
            }
        }
        // TODO: More strict validation of string format
        if (code.startsWith(realEcosystemPrefix) || code.startsWith(testEcosystemPrefix)) {
            String num = code.split("#")[1];
            Long val = Long.parseLong(num);
            return new CurrencyID(val);
        }
        return null;
    }
}
