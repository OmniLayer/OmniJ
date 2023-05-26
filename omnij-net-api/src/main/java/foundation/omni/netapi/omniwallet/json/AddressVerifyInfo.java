package foundation.omni.netapi.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.base.Address;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 */
public class AddressVerifyInfo {
    private final String balance;
    private final String reservedBalance;
    private final Address address;
    private final boolean isFrozen;

    public AddressVerifyInfo(@JsonProperty("balance") String balance,
                             @JsonProperty("reserved") String reservedBalance, // For deprecated www.omniexplorer.org endpoint this was "reserved_balance"
                             @JsonProperty("address") Address address,
                             @JsonProperty("frozen") Boolean frozen) {
        this.balance = balance;
        this.reservedBalance = reservedBalance;
        this.address = address;
        this.isFrozen = (frozen != null) ? frozen : false;
    }

    public String getBalance() {
        return balance;
    }

    public String getReservedBalance() {
        return reservedBalance;
    }

    public Address getAddress() {
        return address;
    }

    public boolean isFrozen() {
        return isFrozen;
    }
}
