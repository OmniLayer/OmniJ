package foundation.omni.tx;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;

/**
 * Interface for transaction fee calculator
 */
public interface FeeCalculator {
    public Coin calculateFee(Transaction tx);
}
