package foundation.omni.tx;

import org.bitcoinj.core.Transaction;

/**
 *
 */
public class OmniTransactionParser {

    OmniTransaction parse(Transaction btcTx) {
        OmniTransaction omniTx = new OmniTransaction(btcTx);
        return omniTx;
    }
}
