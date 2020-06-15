package foundation.omni.netapi.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 *
 * Note that Omniwallet currently can return a zero balance when an error occurs.
 * To detect this you must check and detect that `error` is true.
 */
public class OmniwalletAddressPropertyBalance {
    private final CurrencyID  id;   // unfortunately, can be string or number (in case of 0)
    private final String      symbol;
    private final OmniValue   value;
    private final boolean     divisible;
    private final OmniValue   pendingpos;
    private final OmniValue   pendingneg;
    private final boolean     error;

    public OmniwalletAddressPropertyBalance(@JsonProperty("id") Object id,
                                            @JsonProperty("symbol") String symbol,
                                            @JsonProperty("value") String value,
                                            @JsonProperty("divisible") boolean divisible,
                                            @JsonProperty("pendingpos") String pendingpos,
                                            @JsonProperty("pendingneg") String pendingneg,
                                            @JsonProperty("error") Boolean error) {
        PropertyType propType = PropertyType.of(divisible);
        this.id = CurrencyID.of((id instanceof String) ? Long.parseLong((String) id) : ((Integer) id).longValue());
        this.symbol = symbol;
        this.value = OmniValue.ofWilletts(Long.parseLong(value), propType);
        this.divisible = divisible;
        this.pendingpos = OmniValue.ofWilletts(Long.parseLong(pendingpos), propType);
        this.pendingneg = OmniValue.ofWilletts(Long.parseLong(pendingneg), propType);
        this.error = (error != null) ? error : false;
    }

    public CurrencyID getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public OmniValue getValue() {
        return value;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public OmniValue getPendingpos() {
        return pendingpos;
    }

    public OmniValue getPendingneg() {
        return pendingneg;
    }

    public boolean isError() {
        return error;
    }
}
