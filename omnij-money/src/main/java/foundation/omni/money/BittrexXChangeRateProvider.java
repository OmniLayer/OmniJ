package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Bittrex wrapper
 */
public class BittrexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"OMNI/BTC", "MAID/BTC", "AMP/BTC", "AGRS/BTC", "SAFEX/BTC", "PDC/BTC", "BTC/USDT"};
    static private final String xchangeClassName = "org.knowm.xchange.bittrex.BittrexExchange";

    public BittrexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(xchangeClassName, scheduledExecutorService, pairsConvert(pairs));
    }

    public BittrexXChangeRateProvider() {
        super(xchangeClassName, null,pairsConvert(pairs));
    }
}
