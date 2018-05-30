package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Kraken wrapper
 *
 * @deprecated Use BaseXChangeExchangeRateProvider (or equivalent) directly
 */
@Deprecated
public class KrakenXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"BTC/USD", "USDT/USD"};
    static private final String xchangeClassName = "org.knowm.xchange.kraken.KrakenExchange";

    public KrakenXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(xchangeClassName, scheduledExecutorService, pairsConvert(pairs));
    }

    public KrakenXChangeRateProvider() {
        super(xchangeClassName, null,pairsConvert(pairs));
    }
}
