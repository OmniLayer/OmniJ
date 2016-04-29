package foundation.omni.money;

import foundation.omni.CurrencyID;
import foundation.omni.PropertyType;
import static foundation.omni.PropertyType.*;

import javax.money.CurrencyUnit;

/**
 * CurrencyCode for base and popular OMNI currencies
 * Base is OMNI and TOMNI
 * Others are currencies with market cap over 1M and listed on exchanges
 */
public enum OmniCurrencyCode {
    BTC(CurrencyID.BTC, DIVISIBLE),
    OMNI(CurrencyID.MSC, DIVISIBLE),
    TOMNI(CurrencyID.TMSC, DIVISIBLE),
    MAID(CurrencyID.MAID, INDIVISIBLE),
    USDT(CurrencyID.USDT, DIVISIBLE),
    AMP(CurrencyID.AMP, DIVISIBLE),
    SEC(CurrencyID.SEC, INDIVISIBLE),
    AGRS(CurrencyID.AGRS, DIVISIBLE);

    public static String realEcosystemPrefix = "OMNI_SPT#";
    public static String testEcosystemPrefix = "TOMNI_SPT#";
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
        if (value == CurrencyID.BTC_VALUE) {
            return BTC.name();
        } else if (value == CurrencyID.MSC_VALUE) {
            return OMNI.name();
        } else if (value == CurrencyID.TMSC_VALUE) {
            return TOMNI.name();
        } else if (value == CurrencyID.MAID_VALUE) {
            return MAID.name();
        } else if (value == CurrencyID.USDT_VALUE) {
            return USDT.name();
        } else if (value == CurrencyID.AMP_VALUE) {
            return AMP.name();
        } else if (value == CurrencyID.SEC_VALUE) {
            return SEC.name();
        } else if (value == CurrencyID.AGRS_VALUE) {
            return AGRS.name();
        } else if (CurrencyID.isValidReal(value)) {
            return realEcosystemPrefix + value;
        }
        return testEcosystemPrefix + value;
    }
}
