package foundation.omni.money;

import org.knowm.xchange.poloniex.PoloniexExchange;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Poloniex wrapper
 */
public class PoloniexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public PoloniexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        this(); // Until we upgrade to XChange 4.2.1, call no-arg constructor
        //super(PoloniexExchange.class, scheduledExecutorService,"OMNI/BTC", "MAID/BTC", "AMP/BTC", "BTC/USDT");
    }

    public PoloniexXChangeRateProvider() {
        super(PoloniexExchange.class, "OMNI/BTC", "MAID/BTC", "AMP/BTC", "BTC/USDT");
    }

}
