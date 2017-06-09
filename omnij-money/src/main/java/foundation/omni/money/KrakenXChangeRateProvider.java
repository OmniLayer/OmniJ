package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;
import org.knowm.xchange.kraken.KrakenExchange;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Kraken wrapper
 */
public class KrakenXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public KrakenXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        this(); // Until we upgrade to XChange 4.2.1, call no-arg constructor
        //super(KrakenExchange.class, scheduledExecutorService, "BTC/USD" /*, "USDT/USD" */);
    }

    public KrakenXChangeRateProvider() {
        super(KrakenExchange.class, "BTC/USD" /*, "USDT/USD" */);
    }
}
