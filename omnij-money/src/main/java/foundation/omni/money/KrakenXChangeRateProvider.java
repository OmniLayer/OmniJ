package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;
import org.knowm.xchange.kraken.KrakenExchange;

/**
 * Kraken wrapper
 */
public class KrakenXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public KrakenXChangeRateProvider() {
        super(KrakenExchange.class, "BTC/USD" /*, "USDT/USD" */);
    }
}
