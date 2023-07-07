package foundation.omni.tx;

import org.bitcoinj.base.Coin;
import org.bitcoinj.core.Transaction;

/**
 * Trivial fee calculator that always uses the recommended minimum fee
 * Provides backwards compatibility with previous implementations of OmniTxBuilder
 */
public class DefaultFixedFeeCalculator implements FeeCalculator {
    @Override
    public Coin calculateFee(Transaction tx) {
        return Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
    }
}
