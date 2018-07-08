package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.OmniValue;
import org.bitcoinj.core.Address;

import java.math.BigDecimal;

/**
 *  Balance entry for an Address
 */
public class AddressBalanceEntry extends BalanceEntry {
    private final Address address;
    private final OmniValue frozen;

    public AddressBalanceEntry(@JsonProperty("address") Address address,
                               @JsonProperty("balance") OmniValue balance,
                               @JsonProperty("reserved") OmniValue reserved,
                               @JsonProperty("frozen") OmniValue frozen) {
        super(balance,reserved);
        this.address = address;
        this.frozen = frozen;
    }

    public Address getAddress() {
        return address;
    }
}
