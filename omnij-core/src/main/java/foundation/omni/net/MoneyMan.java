package foundation.omni.net;

import foundation.omni.OmniDivisibleValue;
import org.bitcoinj.base.Coin;

/**
 * Definitions for MoneyMan address functionality on TestNet and RegTest
 */
public class MoneyMan {
    /** Exchange rate for MoneyMan transactions: 100 Omni per BTC */
    public static final long willettsPerSatoshi = 100;

    /**
     * Calculate Omni returned in a MoneyMan exchange.
     *
     * @param bitcoin An amount of bitcoin
     * @return the amount of Omni the MoneyMan will exchange it for
     */
    public static OmniDivisibleValue toOmni(Coin bitcoin) {
        return OmniDivisibleValue.ofWilletts(bitcoin.value * willettsPerSatoshi);
    }

    /**
     * Calculate Bitcoin required for a MoneyMan exchange. Note that the number of OMNI/TOMNI returned
     * may be greater than the amount requested, because this calculation ensures <b>at least</b>
     * the amount requested will be returned.
     *
     * @param omni A desired amount of Omni
     * @return the amount of Bitcoin the MoneyMan will require for it
     */
    public static Coin requiredBitcoin(OmniDivisibleValue omni) {
        Coin[] res = Coin.valueOf(omni.getWilletts()).divideAndRemainder(willettsPerSatoshi);
        return (res[1].value > 0)
                ? res[0].plus(Coin.SATOSHI)     // if there's a remainder add 1 satoshi
                : res[0];
    }
}
