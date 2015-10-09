package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;

import java.math.BigDecimal;

/**
 *  Balance entry for an Address
 */
public class AddressBalanceEntry extends BalanceEntry {
    private final Address address;

    public AddressBalanceEntry(@JsonProperty("address") Address address,
                               @JsonProperty("balance") BigDecimal balance,
                               @JsonProperty("reserved") BigDecimal reserved) {
        super(balance,reserved);
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }
}
