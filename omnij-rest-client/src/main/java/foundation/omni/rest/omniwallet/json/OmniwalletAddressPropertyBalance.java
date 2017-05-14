package foundation.omni.rest.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 */
public class OmniwalletAddressPropertyBalance {
    private final Object      id;   // unfortunately, can be string or number (in case of 0)
    private final String      symbol;
    private final String      value;
    private final boolean     divisible;
    private final String      pendingpos;
    private final String      pendingneg;

    public OmniwalletAddressPropertyBalance(@JsonProperty("id") Object id,
                                            @JsonProperty("symbol") String symbol,
                                            @JsonProperty("value") String value,
                                            @JsonProperty("divisible") boolean divisible,
                                            @JsonProperty("pendingpos") String pendingpos,
                                            @JsonProperty("pendingneg") String pendingneg) {
        this.id = id;
        this.symbol = symbol;
        this.value = value;
        this.divisible = divisible;
        this.pendingpos = pendingpos;
        this.pendingneg = pendingneg;
    }

    public Object getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getValue() {
        return value;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public String getPendingpos() {
        return pendingpos;
    }

    public String getPendingneg() {
        return pendingneg;
    }
}
