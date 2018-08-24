package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;

/**
 * Balance entry for an property/currency
 */
public class PropertyBalanceEntry extends BalanceEntry {
    private final CurrencyID propertyid;
    private final String     name;

    public PropertyBalanceEntry(@JsonProperty("propertyid") CurrencyID propertyid,
                                @JsonProperty("name") String name,
                                @JsonProperty("balance") OmniValue balance,
                                @JsonProperty("reserved") OmniValue reserved,
                                @JsonProperty("frozen") OmniValue frozen) {
        super(balance, reserved, frozen);
        this.propertyid = propertyid;
        this.name = name;
    }

    public CurrencyID getPropertyid() {
        return propertyid;
    }

    public String getName() {
        return name;
    }
}
