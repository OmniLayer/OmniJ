package foundation.omni.money;

import org.javamoney.moneta.CurrencyUnitBuilder;

import javax.money.CurrencyContext;
import javax.money.CurrencyContextBuilder;
import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.CurrencyProviderSpi;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * First cut at Omni Currency Provider
 * First version is OMNI only, other currencies will be added soon
 */
public class OmniCurrencyProvider implements CurrencyProviderSpi {
    final static int divisbleFractionDigits = 8;
    final static int indivisbleFractionDigits = 0;

    private final CurrencyContext CONTEXT = CurrencyContextBuilder.of("OmniCurrencyContextProvider")
            .build();

    private Set<CurrencyUnit> omniSet = new HashSet<>();


    public OmniCurrencyProvider() {
        CurrencyUnit omniUnit = CurrencyUnitBuilder.of("OMNI", CONTEXT)
                .setDefaultFractionDigits(divisbleFractionDigits)
                .build();
        omniSet.add(omniUnit);
        omniSet = Collections.unmodifiableSet(omniSet);
    }
    @Override
    public String getProviderName(){
        return "bitcoin";
    }

    /**
     * Return a {@link CurrencyUnit} instances matching the given
     * {@link javax.money.CurrencyContext}.
     *
     * @param query the {@link javax.money.CurrencyQuery} containing the parameters determining the query. not null.
     * @return the corresponding {@link CurrencyUnit}s matching, never null.
     */
    @Override
    public Set<CurrencyUnit> getCurrencies(CurrencyQuery query){
        // only ensure OMNI is the code, or it is a default query.
        if(query.getCurrencyCodes().contains("OMNI") ||  query.getCurrencyCodes().isEmpty()){
            return omniSet;
        }
        return Collections.emptySet();
    }

    @Override
    public boolean isCurrencyAvailable(CurrencyQuery query) {
        return !getCurrencies(query).isEmpty();
    }
}
