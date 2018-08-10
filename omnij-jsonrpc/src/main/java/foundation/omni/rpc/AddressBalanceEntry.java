package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;
import foundation.omni.OmniValue;

/**
 *  Balance entry for an Address
 */
public class AddressBalanceEntry extends BalanceEntry {
    private final Address address;

    public AddressBalanceEntry(@JsonProperty("address") Address address,
                               @JsonProperty("balance") OmniValue balance,
                               @JsonProperty("reserved") OmniValue reserved,
                               @JsonProperty("frozen") OmniValue frozen) {
        super(balance, reserved, frozen);
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }
}
