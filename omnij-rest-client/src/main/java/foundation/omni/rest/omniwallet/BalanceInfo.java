package foundation.omni.rest.omniwallet;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;

/**
 *
 */
public class BalanceInfo {
    public CurrencyID id;
    public OmniValue value;
    //String symbol;
    //OmniValue pendingpos;
    //OmniValue pendingneg;
// Could be simplified to the following
//    OmniAmount  amount;
//    OmniValue   pendingpos;
//    OmniValue   pendingneg;
}
