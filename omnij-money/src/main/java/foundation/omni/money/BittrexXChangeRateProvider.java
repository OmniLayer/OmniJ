package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;
import org.knowm.xchange.bittrex.v1.BittrexExchange;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Bittrex wrapper
 */
public class BittrexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public BittrexXChangeRateProvider(ScheduledExecutorService scheduledExecutorService) {
        this(); // Until we upgrade to XChange 4.2.1, call no-arg constructor
        //super(BittrexExchange.class, scheduledExecutorService, "OMNI/BTC", "MAID/BTC", "AMP/BTC", "AGRS/BTC", "SEC/BTC", "PDC/BTC", "BTC/USDT");
    }

    public BittrexXChangeRateProvider() {
        super(BittrexExchange.class, "OMNI/BTC", "MAID/BTC", "AMP/BTC", "AGRS/BTC", "SEC/BTC", "PDC/BTC", "BTC/USDT");
    }
}
