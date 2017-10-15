package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Poloniex wrapper
 *
 * @deprecated Use BaseXChangeExchangeRateProvider (or equivalent) directly
 */
@Deprecated
public class PoloniexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"OMNI/BTC", "MAID/BTC", "AMP/BTC", "BTC/USDT"};
    static private final String xchangeClassName = "org.knowm.xchange.poloniex.PoloniexExchange";

    public PoloniexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(xchangeClassName, scheduledExecutorService, pairsConvert(pairs));
    }

    public PoloniexXChangeRateProvider() {
        super(xchangeClassName, null, pairsConvert(pairs));
    }
}
