package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;
import org.knowm.xchange.bittrex.v1.BittrexExchange;

/**
 * Bittrex wrapper
 */
public class BittrexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public BittrexXChangeRateProvider() {
        super(BittrexExchange.class, "OMNI/BTC", "MAID/BTC", "AMP/BTC", "AGRS/BTC", "SEC/BTC", "PDC/BTC", "BTC/USDT");
    }
}
