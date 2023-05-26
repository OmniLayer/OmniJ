package foundation.omni.tx;

import org.bitcoinj.base.Coin;
import org.bitcoinj.core.Transaction;

/**
 * Interface for transaction fee calculator
 */
public interface FeeCalculator {
    /**
     * Calculate the fee for a transaction
     * @param tx An almost-final transaction
     * @return the calculated fee amount
     */
    public Coin calculateFee(Transaction tx);
}
