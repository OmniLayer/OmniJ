package foundation.omni.money;


import java.math.BigDecimal;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ProviderContext;
import javax.money.convert.ProviderContextBuilder;
import javax.money.convert.RateType;

import com.msgilligan.bitcoinj.money.CurrencyUnitPair;
import com.msgilligan.bitcoinj.money.ExchangeRateChange;
import com.msgilligan.bitcoinj.money.ExchangeRateObserver;
import com.msgilligan.bitcoinj.money.ObservableExchangeRateProvider;
import org.javamoney.moneta.CurrencyUnitBuilder;
import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.DefaultNumberValue;

/**
 * Based on Moneta Identity provider licensed under Apache license
 */
public class TetherUSDIdentityProvider extends AbstractRateProvider implements ObservableExchangeRateProvider {

    private CurrencyUnit base = Monetary.getCurrency("USDT");
    private CurrencyUnit target= Monetary.getCurrency("USD");

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

    private ExchangeRateChange buildExchangeRateChange() {
        return new ExchangeRateChange(buildExchangeRate(), 0);

    }

    private ExchangeRate buildExchangeRate() {
        ExchangeRateBuilder builder = new ExchangeRateBuilder(getContext().getProviderName(), RateType.OTHER)
                .setBase(base);
        builder.setTerm(target);
        builder.setFactor(DefaultNumberValue.of(BigDecimal.ONE));
        return builder.build();

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

    @Override
    public void registerExchangeRateObserver(CurrencyUnitPair pair, ExchangeRateObserver observer) {
        // Call the observer once to set the fixed exchange rate
        observer.onExchangeRateChange(buildExchangeRateChange());
    }

    @Override
    public void start() {
        // NOOP because rate never changes
    }

    @Override
    public void stop() {
        // NOOP because rate never changes
    }
}
