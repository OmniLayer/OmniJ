package foundation.omni.money;

import java.math.BigDecimal;

import javax.money.convert.ConversionQuery;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ProviderContext;
import javax.money.convert.ProviderContextBuilder;
import javax.money.convert.RateType;

import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.DefaultNumberValue;

/**
 * A {@link javax.money.convert.ExchangeRateProvider} that provides a fixed 1-to-1 mapping of USDT to USD.
 * Note that on real-world exchanges USDT can fluctuate in price relative to USD.
 * Based on Moneta Identity provider.
 */
public class TetherUSDIdentityProvider extends AbstractRateProvider {

    /**
     * The {@link javax.money.convert.ConversionContext} of this provider.
     */
    private static final ProviderContext CONTEXT =
            ProviderContextBuilder.of("TETHERIDENT", RateType.OTHER).set("providerDescription", "Tether Identity Provider").build();

    /**
     * Constructor, also loads initial data.
     */
    public TetherUSDIdentityProvider() {
        super(CONTEXT);
    }
    
    /**
     * Check if this provider can provide a rate, which is only the case if base and term are equal.
     *
     * @param conversionQuery the required {@link ConversionQuery}, not {@code null}
     * @return true, if the contained base and term currencies are known to this provider.
     */
    @Override
    public boolean isAvailable(ConversionQuery conversionQuery) {
        return conversionQuery.getBaseCurrency().getCurrencyCode().equals("USDT") &&
                conversionQuery.getCurrency().getCurrencyCode().equals("USD");
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        if (isAvailable(conversionQuery)) {
            ExchangeRateBuilder builder = new ExchangeRateBuilder(getContext().getProviderName(), RateType.OTHER)
                    .setBase(conversionQuery.getBaseCurrency());
            builder.setTerm(conversionQuery.getCurrency());
            builder.setFactor(DefaultNumberValue.of(BigDecimal.ONE));
            return builder.build();
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
	 *
	 * @see
	 * javax.money.convert.ExchangeRateProvider#getReversed(javax.money.convert
	 * .ExchangeRate)
	 */
    @Override
    public ExchangeRate getReversed(ExchangeRate rate) {
        if (rate.getContext().getProviderName().equals(CONTEXT.getProviderName())) {
            return new ExchangeRateBuilder(rate.getContext()).setTerm(rate.getBaseCurrency())
                    .setBase(rate.getCurrency()).setFactor(new DefaultNumberValue(BigDecimal.ONE)).build();
        }
        return null;
    }
}
