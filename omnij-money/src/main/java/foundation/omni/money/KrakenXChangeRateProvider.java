package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;
import org.knowm.xchange.kraken.KrakenExchange;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Kraken wrapper
 */
public class KrakenXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"BTC/USD", "USDT/USD"};

    public KrakenXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(KrakenExchange.class, scheduledExecutorService, pairs);
    }

    public KrakenXChangeRateProvider() {
        super(KrakenExchange.class, pairs);
    }
}
