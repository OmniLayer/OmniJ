package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;

/**
 * Balance entry for an property/currency
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyBalanceEntry extends BalanceEntry {
    private final CurrencyID propertyid;

    public PropertyBalanceEntry(@JsonProperty("propertyid") CurrencyID propertyid,
                                @JsonProperty("balance") OmniValue balance,
                                @JsonProperty("reserved") OmniValue reserved,
                                @JsonProperty("frozen") OmniValue frozen) {
        super(balance, reserved, frozen);
        this.propertyid = propertyid;
    }

    public CurrencyID getPropertyid() {
        return propertyid;
    }
}
