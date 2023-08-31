package foundation.omni.tx;

import org.bitcoinj.core.Transaction;

/**
 * @deprecated This code is incomplete, doesn't work, and will be replaced.
 */
@Deprecated
public class OmniTransactionParser {

    OmniTransaction parse(Transaction btcTx) {
        OmniTransaction omniTx = new OmniTransaction(btcTx);
        return omniTx;
    }
}
