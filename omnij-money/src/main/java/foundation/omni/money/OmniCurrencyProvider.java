package foundation.omni.money;

import foundation.omni.PropertyType;
import org.javamoney.moneta.CurrencyUnitBuilder;

import javax.money.CurrencyContext;
import javax.money.CurrencyContextBuilder;
import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.CurrencyProviderSpi;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static foundation.omni.money.OmniCurrencyCode.*;
/**
 * First cut at Omni Currency Provider
 * First version is OMNI only, other currencies will be added soon
 */
public class OmniCurrencyProvider implements CurrencyProviderSpi {
    final static int divisbleFractionDigits = 8;
    final static int indivisbleFractionDigits = 0;

    final private static Set<OmniCurrencyCode> codes = new HashSet<>(Arrays.asList(OMNI, MAID, USDT, AMP, SEC, AGRS));

    private final CurrencyContext CONTEXT = CurrencyContextBuilder.of("OmniCurrencyContextProvider")
            .build();

    private final Set<CurrencyUnit> omniSet;


    public OmniCurrencyProvider() {
        Set<CurrencyUnit> set = new HashSet<>();
        for (OmniCurrencyCode code : codes) {
            set.add(build(code));
        }
        omniSet = Collections.unmodifiableSet(set);
    }

    @Override
    public String getProviderName(){
        return "omni";
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
        // Empty matches everything, return all supported currencies.
        if (query.getCurrencyCodes().isEmpty()) {
            return omniSet;
        }
        // Build a response with all matching currencies
        Set<CurrencyUnit> response = new HashSet<>();
        for (String code : query.getCurrencyCodes()) {
            CurrencyUnit unit = find(code);
            if (unit != null) {
                response.add(unit);
            }
        }
        return response;
    }

    @Override
    public boolean isCurrencyAvailable(CurrencyQuery query) {
        return !getCurrencies(query).isEmpty();
    }

    private CurrencyUnit find(String code) {
        for (CurrencyUnit unit : omniSet) {
            if (unit.getCurrencyCode().equals(code)) {
                return unit;
            }
        }
        return null;
    }

    private CurrencyUnit build(OmniCurrencyCode code) {
        return CurrencyUnitBuilder.of(code.name(), CONTEXT)
                .setDefaultFractionDigits(code.type() == PropertyType.DIVISIBLE ? divisbleFractionDigits : indivisbleFractionDigits)
                .build();
    }
}
