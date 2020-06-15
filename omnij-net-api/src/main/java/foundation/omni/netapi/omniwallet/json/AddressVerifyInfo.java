package foundation.omni.netapi.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 */
public class AddressVerifyInfo {
    private final String balance;
    private final String reservedBalance;
    private final Address address;

    public AddressVerifyInfo(@JsonProperty("balance") String balance,
                             @JsonProperty("reserved_balance") String reservedBalance,
                             @JsonProperty("address") Address address) {
        this.balance = balance;
        this.reservedBalance = reservedBalance;
        this.address = address;
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
}
