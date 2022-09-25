package foundation.omni.json.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import foundation.omni.CurrencyID;
import foundation.omni.json.conversion.CurrencyIDKeyDeserializer;
import foundation.omni.json.conversion.CurrencyIDStringSerializer;
import foundation.omni.BalanceEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the balances for all properties for a given Address
 */
@JsonSerialize(keyUsing = CurrencyIDStringSerializer.class)
@JsonDeserialize(keyUsing = CurrencyIDKeyDeserializer.class)
public class WalletAddressBalance extends ConcurrentHashMap<CurrencyID, BalanceEntry> {
    public WalletAddressBalance() {
    }

    public WalletAddressBalance(Map<CurrencyID, BalanceEntry> m) {
        super(m);
    }
}
