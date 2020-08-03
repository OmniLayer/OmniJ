package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import foundation.omni.OmniValue;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Balance data for a specific Omni CurrencyID in a single Bitcoin address
 * TODO: Move to omnij-core, remove Jackson annotations, write Serializer/Deserializer?
 * (move along with AddressBalanceEntry, PropertyBalanceEntry)
 */
public class BalanceEntry implements Iterable<OmniValue>  {
    protected final OmniValue balance;
    protected final OmniValue reserved;
    protected final OmniValue frozen;

    @JsonCreator
    public BalanceEntry(@JsonProperty("balance") OmniValue balance,
                        @JsonProperty("reserved") OmniValue reserved,
                        @JsonProperty("frozen") OmniValue frozen) {
        this.balance = balance;
        this.reserved = reserved;
        this.frozen = (frozen != null) ? frozen : OmniValue.of(0, balance.getPropertyType());
    }

    public OmniValue getBalance() {
        return balance;
    }

    public OmniValue getReserved() {
        return reserved;
    }

    public OmniValue getFrozen() {
        return frozen;
    }

    public static OmniValue totalBalance(BalanceEntry balanceEntry) {
        return balanceEntry.balance
                .plus(balanceEntry.reserved)
                .plus(balanceEntry.frozen);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BalanceEntry that = (BalanceEntry) o;

        return  balance.equals(that.balance) &&
                reserved.equals(that.reserved) &&
                frozen.equals(that.frozen);
    }

    @Override
    public int hashCode() {
        int result = balance.hashCode();
        result = 31 * result + reserved.hashCode();
        result = 31 * result + frozen.hashCode();
        return result;
    }


    /**
     * Useful for Spock tests
     *
     * @return an iterator that will iterate the three OmniValue fields
     */
    @Override
    public Iterator<OmniValue> iterator() {
        return Arrays.asList(balance, reserved, frozen).iterator();
    }

    @Override
    public String toString() {
        return "[balance: " + balance.toJsonFormattedString() +
                ", reserved: " + reserved.toJsonFormattedString() +
                ", frozen: " + frozen.toJsonFormattedString() + "]";
    }

}
