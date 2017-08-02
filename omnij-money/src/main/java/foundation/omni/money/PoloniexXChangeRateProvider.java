package foundation.omni.money;

import org.knowm.xchange.poloniex.PoloniexExchange;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Poloniex wrapper
 */
public class PoloniexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"OMNI/BTC", "MAID/BTC", "AMP/BTC", "BTC/USDT"};

    public PoloniexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(PoloniexExchange.class, scheduledExecutorService,pairs);
    }

    public PoloniexXChangeRateProvider() {
        super(PoloniexExchange.class, pairs);
    }

}
