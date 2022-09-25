package foundation.omni.json.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.PropertyType;
import org.bitcoinj.core.Address;
import org.consensusj.bitcoin.json.conversion.AddressKeyDeserializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Balances for all currencies for a list of addresses
 */
@JsonDeserialize(keyUsing = AddressKeyDeserializer.class)
public class OmniJBalances extends ConcurrentHashMap<Address, WalletAddressBalance> {

    /**
     * @param propertyID propertyId to count
     * @param type  PropertyType of CurrencyID
     * @return count of all currency/property of specified type
     */
    public OmniValue countCurrency(CurrencyID propertyID, PropertyType type) {
        final OmniValue zero = OmniValue.of(0, type);
        return entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream())
                .filter(entry -> entry.getKey().equals(propertyID))
                .map(e -> e.getValue().getBalance())
                .reduce(OmniValue::plus)
                .orElse(zero);
    }
}
