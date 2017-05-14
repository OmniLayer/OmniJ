package foundation.omni.rest.omniwallet.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson POJO wrapper for raw Omniwallet response
 */
public class AddressVerifyInfo {
    private final String balance;
    private final String reservedBalance;
    private final String address;

    public AddressVerifyInfo(@JsonProperty("balance") String balance,
                             @JsonProperty("reserved_balance") String reservedBalance,
                             @JsonProperty("address") String address) {
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

    public String getAddress() {
        return address;
    }
}
