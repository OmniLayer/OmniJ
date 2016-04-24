package foundation.omni.money;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.poloniex.PoloniexExchange;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;

/**
 * Exchange rate provider currently hardcoded for OMNIBTC only
 */
public class PoloniexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public PoloniexXChangeRateProvider() {
        super(PoloniexExchange.class, "OMNI", "BTC");
    }

}
