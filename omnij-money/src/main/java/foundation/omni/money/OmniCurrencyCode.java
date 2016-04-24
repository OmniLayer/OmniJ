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
    OMNI(CurrencyID.MSC, DIVISIBLE),
    TOMNI(CurrencyID.TMSC, DIVISIBLE),
    MAID(CurrencyID.MAID, INDIVISIBLE),
    USDT(CurrencyID.USDT, DIVISIBLE),
    AMP(CurrencyID.AMP, DIVISIBLE),
    SEC(CurrencyID.SEC, INDIVISIBLE),
    AGRS(CurrencyID.AGRS, DIVISIBLE);

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
}
