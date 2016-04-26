package foundation.omni.money;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.poloniex.PoloniexExchange;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;

/**
 * Poloniex wrapper
 */
public class PoloniexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public PoloniexXChangeRateProvider() {
        super(PoloniexExchange.class, "OMNI/BTC", "MAID/BTC", "AMP/BTC", "BTC/USDT");
    }

}
