package foundation.omni.rest.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 */
public class PropertyVerifyInfo {
    private final long currencyID;
    private final boolean divisible;
    private final String protocol;
    private final String name;

    public PropertyVerifyInfo(@JsonProperty("currencyID") long currencyID,
                              @JsonProperty("divisible") boolean divisible,
                              @JsonProperty("Protocol") String protocol,
                              @JsonProperty("name") String name) {
        this.currencyID = currencyID;
        this.divisible = divisible;
        this.protocol = protocol;
        this.name = name;
    }

    public long getCurrencyID() {
        return currencyID;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getName() {
        return name;
    }
}
