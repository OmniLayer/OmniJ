package foundation.omni.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bitcoinj.core.Address;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Balance data for a specific Omni CurrencyID in a single Bitcoin address
 */
public class BalanceEntry implements Iterable<BigDecimal>  {
    protected final BigDecimal balance;     // TODO: balance and reserved should be type OmniValue
    protected final BigDecimal reserved;

    @JsonCreator
    public BalanceEntry(@JsonProperty("balance") BigDecimal balance,
                        @JsonProperty("reserved") BigDecimal reserved) {
        this.balance = balance;
        this.reserved = reserved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BalanceEntry that = (BalanceEntry) o;

        if (balance.compareTo(that.balance) != 0) return false;
        if (reserved.compareTo(that.reserved) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = balance.hashCode();
        result = 31 * result + reserved.hashCode();
        return result;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    /**
     * Useful for Spock tests
     *
     * @return an iterator that will iterate the two BigDecimal fields
     */
    @Override
    public Iterator<BigDecimal> iterator() {
        return Arrays.asList(balance, reserved).iterator();
    }

    @Override
    public String toString() {
        return "[balance: " + balance + ", reserved: " + reserved + " ]";
    }

}
