package foundation.omni.rpc;

import org.bitcoinj.core.Address;

import java.math.BigDecimal;

/**
 * Balance data for a specific Omni CurrencyID in a single Bitcoin address
 *
 * A Java representation of the JSON entry returned by {@code "omni_getallbalancesforid"}.
 */
public class MPBalanceEntry {
    private Address address;
    private BigDecimal balance;
    private BigDecimal reserved;

    public MPBalanceEntry(Address address, BigDecimal balance, BigDecimal reserved) {
        this.address = address;
        this.balance = balance;
        this.reserved = reserved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MPBalanceEntry that = (MPBalanceEntry) o;

        if (!address.equals(that.address)) return false;
        if (balance.compareTo(that.balance) != 0) return false;
        if (reserved.compareTo(that.reserved) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + balance.hashCode();
        result = 31 * result + reserved.hashCode();
        return result;
    }

    public Address getAddress() {
        return address;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getReserved() {
        return reserved;
    }
}
