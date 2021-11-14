package foundation.omni.netapi;

import foundation.omni.CurrencyID;
import foundation.omni.rpc.BalanceEntry;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class WalletAddressBalance extends HashMap<CurrencyID, BalanceEntry> {
    public WalletAddressBalance() {
    }

    public WalletAddressBalance(Map<CurrencyID, BalanceEntry> m) {
        super(m);
    }
}
