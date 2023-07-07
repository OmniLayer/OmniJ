package foundation.omni;

import org.bitcoinj.base.Address;

/**
 *  Balance entry for an Address
 */
public class AddressBalanceEntry extends BalanceEntry {
    private final Address address;

    public AddressBalanceEntry(Address address,
                               OmniValue balance,
                               OmniValue reserved,
                               OmniValue frozen) {
        super(balance, reserved, frozen);
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }
}
