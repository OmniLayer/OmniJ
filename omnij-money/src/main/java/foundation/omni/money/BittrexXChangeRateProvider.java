package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;
import org.knowm.xchange.bittrex.v1.BittrexExchange;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Bittrex wrapper
 */
public class BittrexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    static private final String[] pairs = {"OMNI/BTC", "MAID/BTC", "AMP/BTC", "AGRS/BTC", "SAFEX/BTC", "PDC/BTC", "BTC/USDT"};

    public BittrexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        super(BittrexExchange.class, scheduledExecutorService, pairs);
    }

    public BittrexXChangeRateProvider() {
        super(BittrexExchange.class, pairs);
    }
}
