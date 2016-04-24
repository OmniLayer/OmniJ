package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;
import org.knowm.xchange.bittrex.v1.BittrexExchange;
import org.knowm.xchange.currency.CurrencyPair;

/**
 *
 */
public class BittrexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public BittrexXChangeRateProvider() {
        super(BittrexExchange.class, "BTC", "OMNI");
    }
}
