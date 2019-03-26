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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BalanceEntry that = (BalanceEntry) o;

        if (balance.compareTo(that.balance) != 0) return false;
        if (reserved.compareTo(that.reserved) != 0) return false;
        if (frozen.compareTo(that.frozen) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = balance.hashCode();
        result = 31 * result + reserved.hashCode();
        result = 31 * result + frozen.hashCode();
        return result;
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

    /**
     * Useful for Spock tests
     *
     * @return an iterator that will iterate the two BigDecimal fields
     */
    @Override
    public Iterator<OmniValue> iterator() {
        return Arrays.asList(balance, reserved, frozen).iterator();
    }

    @Override
    public String toString() {
        return "[balance: " + balance + ", reserved: " + reserved + ", frozen: " + frozen + "]";
    }

}
