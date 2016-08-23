package foundation.omni.money;

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider;
import com.msgilligan.bitcoinj.money.CurrencyUnitPair;
import org.knowm.xchange.bittrex.v1.BittrexExchange;
import org.knowm.xchange.currency.CurrencyPair;

/**
 * Bittrex wrapper
 */
public class BittrexXChangeRateProvider extends BaseXChangeExchangeRateProvider {
    public BittrexXChangeRateProvider() {
        super(BittrexExchange.class, "OMNI/BTC", "MAID/BTC", "AMP/BTC", "AGRS/BTC", "SEC/BTC", "PDC/BTC", "BTC/USDT");
    }
}
