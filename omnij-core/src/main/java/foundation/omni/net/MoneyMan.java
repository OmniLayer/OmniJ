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
}
