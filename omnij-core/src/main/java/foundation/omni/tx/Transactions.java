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
     * are less than 100.
     */
    public enum TransactionType {
        SIMPLE_SEND((short) 0, (short) 0),
        SEND_TO_OWNERS((short) 0, (short) 3),
        SELL_OMNI_FOR_BITCOIN((short) 1, (short) 20);

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
