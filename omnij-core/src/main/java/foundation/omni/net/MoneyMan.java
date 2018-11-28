package foundation.omni.net;

import foundation.omni.OmniDivisibleValue;
import org.bitcoinj.core.Coin;

/**
 * Definitions for MoneyMan address functionality on TestNet and RegTest
 */
public class MoneyMan {
    public static final long willettsPerSatoshi = 100;  // Exchange rate for MoneyMan: 100 Omni per BTC

    /**
     * Calculate Omni returned in a MoneyMan exchange.
     *
     * @param bitcoin An amount of bitcoin
     * @return the amount of Omni the MoneyMan will exchange it for
     */
    public static OmniDivisibleValue toOmni(Coin bitcoin) {
        return OmniDivisibleValue.ofWilletts(bitcoin.value * willettsPerSatoshi);
    }
}
