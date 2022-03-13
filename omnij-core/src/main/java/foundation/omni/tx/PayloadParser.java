package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;

import java.nio.ByteBuffer;

/**
 *
 */
public class PayloadParser {
    private final ByteBuffer buffer;

    public PayloadParser(byte[] payload) {
        buffer = ByteBuffer.wrap(payload);
    }

    public static PayloadParser create(byte[] payload) {
        return new PayloadParser(payload);
    }

    public short getVersion() {
        return buffer.getShort();
    }

    public short getTransaction() {
        return buffer.getShort();
    }

    public CurrencyID getCurrencyID() {
        return CurrencyID.ofUnsigned(buffer.getInt());
    }

    public OmniValue getWilletts() {
      // FIXME: hard-coded to divisible!
      return OmniValue.ofWilletts(buffer.getLong(), true);
    }
}
