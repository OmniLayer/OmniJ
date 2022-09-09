package foundation.omni.tx;

import org.bitcoinj.core.Address;

import java.util.Arrays;

/**
 *
 */
public class Transactions {
    /**
     * All Omni Transactions implement this interface
     */
    public interface OmniTx {
        short version();
        TransactionType type();
        byte[] payload();
    }

    /**
     * Omni Transactions that use a reference address implement this interface
     */
    public interface OmniRefTx extends OmniTx {
        Address referenceAddress();
    }

    /**
     * Omni Layer Transaction Type
     * <p>
     * Transaction type is an unsigned 16-bit value, but all currently defined transaction types
     * are less than 100. This is a partial list, see {@code omnicore.h} for the complete list.
     * @see <a href="https://github.com/OmniLayer/omnicore/blob/master/src/omnicore/omnicore.h">enum TransactionType in omnicore.h</a>
     */
    public enum TransactionType {
        SIMPLE_SEND(                (short) 0, (short)  0),
        SEND_TO_OWNERS(             (short) 0, (short)  3),     // TODO: I think transaction version should be 1 here.
        SEND_ALL(                   (short) 0, (short)  4),
        TRADE_OFFER(                (short) 1, (short) 20),
        ACCEPT_OFFER_BTC(           (short) 0, (short) 22),
        METADEX_TRADE(              (short) 0, (short) 25),
        CREATE_PROPERTY_FIXED(      (short) 0, (short) 50),
        CREATE_PROPERTY_VARIABLE(   (short) 1, (short) 51),
        PROMOTE_PROPERTY(           (short) 0, (short) 52),
        CLOSE_CROWDSALE(            (short) 0, (short) 53),
        CREATE_PROPERTY_MANUAL(     (short) 0, (short) 54),
        GRANT_PROPERTY_TOKENS(      (short) 0, (short) 55),
        REVOKE_PROPERTY_TOKENS(     (short) 0, (short) 56),
        CHANGE_ISSUER_ADDRESS(      (short) 0, (short) 70);

        private final short version;
        private final short value;

        TransactionType(short version, short type) {
            this.version = version;
            value = type;
        }

        public static TransactionType valueOf(short code) {
            return Arrays.stream(values())
                    .filter(e -> e.value() == code)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid transaction type code"));
        }

        /**
         * Return the latest defined version for the transaction type
         * @return numeric value of transaction version
         */
        public short version() {
            return version;
        }

        /**
         * @return numeric value of TransactionType
         */
        public short value() {
            return value;
        }

        public int versionType() {
            return version << 16 | (0xFFFF & (int) value);
        }

    }
}
