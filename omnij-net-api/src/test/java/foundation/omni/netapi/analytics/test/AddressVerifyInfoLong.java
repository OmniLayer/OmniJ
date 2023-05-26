package foundation.omni.netapi.analytics.test;

import org.bitcoinj.base.Address;

import java.math.BigDecimal;

/**
 *
 */
public class AddressVerifyInfoLong {
    public final Address address;
    public final long balance;
    public final long reservedBalance;

    AddressVerifyInfoLong(Address address, String balance, String reservedBalance) {
        this.address = address;
        this.balance = (new BigDecimal(balance)).movePointRight(8).longValueExact();
        if (reservedBalance != null) {
            this.reservedBalance = (new BigDecimal(reservedBalance)).movePointRight(8).longValueExact();
        } else {
            this.reservedBalance = 0;
        }
    }

    public static long balanceExtractor(AddressVerifyInfoLong info) {
        return info.balance + info.reservedBalance;
    }
}
