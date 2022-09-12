package foundation.omni.tx;

import org.bitcoinj.core.Address;

import java.util.Arrays;
import java.util.Optional;

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
     * Transaction type is an unsigned 16-bit value, and stored as a Java {@code short} that is treated as unsigned.
     * The {@link #value()} accessor performs the proper conversion and returns an unsigned {@code int}.
     * <p>
     * With <a href="https://openjdk.org/jeps/427">JEP 427: Pattern Matching for Switch</a> it is possible
     * to handle {@code null} as a {@code case}. So if you're using a recent version of Java (with preview enabled) you
     * can handle undefined transaction types in a single {@code switch} statement/expression. If you have an integer with a transaction type code named {@code typeInt}, you can do something like:
     * <pre> {@code
     * Optional<TransactionType> optionalType = TransactionType.find(typeInt);
     * boolean isSend = switch(optionalType.orElse(null)) {
     *     case SIMPLE_SEND, SEND_TO_OWNERS, SEND_ALL -> true;
     *     default -> false;
     *     case null -> false;
     * }
     * }</pre>
     * The {@code default} case represents defined enum constants not handled with explicit cases and the {@code null} case
     * provides a way to handle numeric codes not (yet) defined in the enum.
     * <p>
     * For versions of Java with switch expressions but no pattern matching, this above code can be written as:
     * <pre> {@code
     * Optional<TransactionType> optionalType = TransactionType.find(typeInt);
     * boolean isSend = optionalType.map(t -> switch(t) {
     *     case SIMPLE_SEND, SEND_TO_OWNERS, SEND_ALL -> true;
     *     default -> false;
     * }).orElse(false);
     * }</pre>
     * <p>
     * For even earlier versions of Java (back to Java 9), it can be written as:
     * <pre> {@code
     * Optional<TransactionType> optionalType = TransactionType.find(typeInt);
     * boolean isSend;
     * optionalType.ifPresentOrElse(t -> switch(t) {
     *     case SIMPLE_SEND:
     *     case SEND_TO_OWNERS:
     *     case SEND_ALL:
     *       isSend = true;
     *       break;
     *     default:
     *       isSend = false;
     * }, {
     *  isSend = false;
     * }
     * }</pre>
     * For a Java 8 example see the Java unit test {@code TransactionTypeTest.java}.
     * <p>
     * This is a partial list of transaction types, see {@code omnicore.h} for the complete list.
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
        // This is stored as an unsigned short
        private final short value;

        TransactionType(short version, short type) {
            this.version = version;
            value = type;
        }

        /**
         * Get transaction type by numeric value
         * @param code transaction type integer value
         * @return The correct enum
         * @throws IllegalArgumentException if not found
         */
        public static TransactionType valueOf(int code) throws IllegalArgumentException {
            return find(code)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid transaction type code"));
        }

        /**
         * Find transaction type by numeric value
         * @param code transaction type integer value
         * @return an Optional enum or {@link Optional#empty()} if not found.
         */
        public static Optional<TransactionType> find(int code) {
            return Arrays.stream(values())
                    .filter(e -> e.value() == code)
                    .findFirst();
        }

        /**
         * Return the latest defined version for the transaction type
         * @return numeric value of transaction version
         */
        public short version() {
            return version;
        }

        /**
         * Return transaction type numeric value as an unsigned {@code int}
         * @return numeric value of TransactionType
         */
        public int value() {
            return Short.toUnsignedInt(value);
        }

        /**
         * Return the 16-bit unsigned value in a Java {@code short}.
         * <p>
         * Use this method with caution as Java treats {@code short}s as signed.
         *
         * @return numeric value of TransactionType as an unsigned {@code short}
         */
        public short unsignedShortValue() {
            return value;
        }

        public int versionType() {
            return version << 16 | (0xFFFF & (int) value);
        }

    }
}
