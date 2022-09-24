package foundation.omni;

/**
 * Balance entry for a property/currency
 */
public class PropertyBalanceEntry extends BalanceEntry {
    private final CurrencyID propertyId;
    private final String     name;

    public PropertyBalanceEntry(CurrencyID  propertyid,
                                String      name,
                                OmniValue   balance,
                                OmniValue   reserved,
                                OmniValue   frozen) {
        super(balance, reserved, frozen);
        this.propertyId = propertyid;
        this.name = name;
    }

    public CurrencyID getPropertyId() {
        return propertyId;
    }

    public String getName() {
        return name;
    }
}
