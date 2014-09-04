package org.mastercoin.rpc;

import com.google.bitcoin.core.Address;

import java.math.BigDecimal;

/**
 * Balance data for a specific Mastercoin CurrencyID in a single Bitcoin address
 *
 * A Java representation of the JSON entry returned by getallbalancesforid_MP
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
