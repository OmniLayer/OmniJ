package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;

import java.nio.ByteBuffer;

/**
 *  This builder does not guarantee correctness, it is a thin wrapper around
 *  ByteBuffer to make the transaction building code easy to read and compare to the Omni Spec.
 *  Most applications should use xxx instead which correctly builds each transaction type.
 */
public class PayloadBuilder {
    ByteBuffer buffer;

    private PayloadBuilder(short version, Transactions.TransactionType type) {
        // WARNING: Hardcoded for simple-send
        buffer = ByteBuffer.allocate(2 + 2 + 4 + 8);
        buffer.putShort(version);
        buffer.putShort(type.value());
    }

    private PayloadBuilder(Transactions.TransactionType type) {
        this(type.version(), type);
    }

    /**
     * Create a payload with the default (latest) transaction version
     */
    public static PayloadBuilder create(Transactions.TransactionType type) {
        return new PayloadBuilder(type);
    }

    /**
     * Create a payload with an explicitly set transaction version
     */
    public static PayloadBuilder create(short version, Transactions.TransactionType type) {
        return new PayloadBuilder(version, type);
    }

    public PayloadBuilder putInt32(int value) {
        buffer.putInt(value);
        return this;
    }

    public PayloadBuilder putInt32(CurrencyID currencyID) {
        return putInt32(currencyID.unsignedIntValue());
    }

    public PayloadBuilder putInt64(long value) {
        buffer.putLong(value);
        return this;
    }

    public PayloadBuilder putInt64(OmniValue value) {
        return putInt64(value.getWilletts());
    }

    public PayloadBuilder putString(String string) {
        // Todo: convert string to bytes and append
        return this;
    }

    public byte[] bytes() {
        // Todo: Do we need to make a length adjustment here?
        return buffer.array();
    }
}
