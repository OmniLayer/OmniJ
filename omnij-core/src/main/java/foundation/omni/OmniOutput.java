package foundation.omni;

import org.bitcoinj.base.Address;

/**
 * A record-like container for an amount and a destination address. Intended for {@code omni_sendmany}.
 */
public class OmniOutput {
    private final Address address;
    private final OmniValue amount;

    public OmniOutput(Address address, OmniValue amount) {
        this.address = address;
        this.amount = amount;
    }

    public Address address() {
        return address;
    }

    public OmniValue amount() {
        return amount;
    }
}
