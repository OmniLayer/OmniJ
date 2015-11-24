package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import org.bitcoinj.core.Address;

import java.math.BigDecimal;

/**
 * Balance entry for an property/currency
 */
public class PropertyBalanceEntry extends BalanceEntry {
    private final CurrencyID propertyid;

    public PropertyBalanceEntry(@JsonProperty("propertyid") CurrencyID propertyid,
                               @JsonProperty("balance") OmniValue balance,
                               @JsonProperty("reserved") OmniValue reserved) {
        super(balance,reserved);
        this.propertyid = propertyid;
    }

    public CurrencyID getPropertyid() {
        return propertyid;
    }
}
