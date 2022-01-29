package foundation.omni.txrecords;

import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniValue;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;

public interface TransactionParameters {
    /**
     * All Omni Transactions implement this interface
     */
    interface OmniTx {
        int transactionType();
        default byte[] payload() { return new byte[]{}; }
    }

    /**
     * Omni Transactions that use a reference address implement this interface
     */
    interface OmniRefTx extends OmniTx {
        Address referenceAddress();
    }

    /**
     * Parameters for Simple Send
     * @param referenceAddress the destination address to send tokens to
     * @param currencyId property id of tokens to send
     * @param amount amount of tokens to send
     */
    record SimpleSend(Address referenceAddress, CurrencyID currencyId, OmniValue amount) implements OmniRefTx
    {
        public static int txType = 0;
        @Override public int transactionType() { return txType; }
    }

    /**
     * Parameters for Send To Owners
     * @param currencyId property id of tokens to send
     * @param amount total amount of tokens to send
     */
    record SendToOwners(CurrencyID currencyId, OmniValue amount) implements OmniTx {
        public static int txType = 0;
        @Override public int transactionType() { return txType; }
    }

    record DexSellOffer(CurrencyID currencyId, OmniDivisibleValue amountForSale, Coin amountDesired,
                        Byte paymentWindow, Coin commitmentFee, Byte action) implements OmniTx {
        public static int txType = 0;
        @Override public int transactionType() { return txType; }
    }

    record MetaDexSellOffer(CurrencyID currencyForSale, OmniValue amountForSale, CurrencyID currencyDesired,
                               OmniValue amountDesired, Byte action) implements OmniTx {
        public static int txType = 0;
        @Override public int transactionType() { return txType; }
    }
}
