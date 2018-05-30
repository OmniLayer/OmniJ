package foundation.omni.tx;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;

/**
 * Simple fee calculator that always uses a linear multiple of transaction size
 */
public class SimpleVariableFeeCalculator implements FeeCalculator {
    @Override
    public Coin calculateFee(Transaction tx) {
        long fee = (tx.getMessageSize() * Transaction.DEFAULT_TX_FEE.getValue()) / 1024;
        return Coin.valueOf(fee);
    }
}
