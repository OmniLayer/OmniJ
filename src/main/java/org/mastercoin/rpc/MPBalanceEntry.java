package org.mastercoin.rpc;

import com.google.bitcoin.core.Address;

import java.math.BigDecimal;

/**
 * User: sean
 * Date: 8/10/14
 * Time: 11:35 AM
 */
public class MPBalanceEntry {
    private Address address;
    private BigDecimal balance;
    private BigDecimal reservedByOffer;
    private BigDecimal reservedByAccept;

    public MPBalanceEntry(Address address, BigDecimal balance, BigDecimal reservedByOffer, BigDecimal reservedByAccept) {
        this.address = address;
        this.balance = balance;
        this.reservedByOffer = reservedByOffer;
        this.reservedByAccept = reservedByAccept;
    }

    public Address getAddress() {
        return address;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getReservedByOffer() {
        return reservedByOffer;
    }

    public BigDecimal getReservedByAccept() {
        return reservedByAccept;
    }
}
