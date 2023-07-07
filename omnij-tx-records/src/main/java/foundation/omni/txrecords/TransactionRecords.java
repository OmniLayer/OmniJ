package foundation.omni.txrecords;

import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.OmniValue;
import foundation.omni.tx.PayloadBuilder;
import foundation.omni.tx.PayloadParser;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;

import static foundation.omni.tx.Transactions.TransactionType;
import static foundation.omni.tx.Transactions.TransactionType.METADEX_TRADE;
import static foundation.omni.tx.Transactions.TransactionType.TRADE_OFFER;
import static foundation.omni.tx.Transactions.TransactionType.SEND_TO_OWNERS;
import static foundation.omni.tx.Transactions.TransactionType.SIMPLE_SEND;
import static foundation.omni.tx.Transactions.OmniTx;
import static foundation.omni.tx.Transactions.OmniRefTx;

/**
 * Omni Layer Transactions defined as Java Records.
 * <p>
 * By using Java Records (available in Java 16+) and {@link PayloadBuilder} we are able
 * to relatively tersely define the transactions and their payload format in a single file.
 *
 */
public interface TransactionRecords {

    /**
     * Parameters for Simple Send
     * @param referenceAddress the destination address to send tokens to
     * @param currencyId property id of tokens to send
     * @param amount amount of tokens to send
     */
    record SimpleSend(short version, Address referenceAddress, CurrencyID currencyId, OmniValue amount) implements OmniRefTx
    {
        public SimpleSend(Address referenceAddress, CurrencyID currencyID, OmniValue amount) {
            this(SIMPLE_SEND.version(), referenceAddress, currencyID, amount);
        }
        @Override public TransactionType type() { return SIMPLE_SEND; }
        @Override public byte[] payload() {
            return PayloadBuilder.create(SIMPLE_SEND)
                    .putInt32(currencyId)
                    .putInt64(amount)
                    .bytes();
        }
        public static SimpleSend of(byte[] payload, Address address) {
            PayloadParser parser = PayloadParser.create(payload);
            short version       = parser.getVersion();
            short transaction   = parser.getTransaction();
            CurrencyID currency = parser.getCurrencyID();
            OmniValue amount    = parser.getWilletts();
            return new SimpleSend(version, address, currency, amount);
        }
    }

    /**
     * Parameters for Send To Owners
     * @param currencyId property id of tokens to send
     * @param amount total amount of tokens to send
     */
    record  SendToOwners(short version, CurrencyID currencyId, OmniValue amount) implements OmniTx {
        @Override public TransactionType type() { return SEND_TO_OWNERS; }
        @Override public byte[] payload() {
            return PayloadBuilder.create(SEND_TO_OWNERS)
                    .putInt32(currencyId)
                    .putInt64(amount)
                    .bytes();
        }
    }

    record DexSellOffer(short version, CurrencyID currencyId, OmniDivisibleValue amountForSale, Coin amountDesired,
                        Byte paymentWindow, Coin commitmentFee, Byte action) implements OmniTx {
        @Override public TransactionType type() { return TRADE_OFFER; }
        @Override public byte[] payload() {
            return PayloadBuilder.create(TRADE_OFFER)
                    .putInt32(currencyId)
                    .putInt64(amountForSale)
                    .bytes();
        }
    }

    record MetaDexSellOffer(short version, CurrencyID currencyForSale, OmniValue amountForSale, CurrencyID currencyDesired,
                               OmniValue amountDesired, Byte action) implements OmniTx {
        @Override public TransactionType type() { return METADEX_TRADE; }
        @Override public byte[] payload() {
            return PayloadBuilder.create(METADEX_TRADE)
                    .putInt32(currencyForSale)
                    .putInt64(amountForSale)
                    .bytes();
        }
    }
}
