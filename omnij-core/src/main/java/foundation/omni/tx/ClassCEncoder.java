package foundation.omni.tx;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

import java.nio.charset.StandardCharsets;

/**
 * WIP Class-C Encoder
 */
public class ClassCEncoder {
    private static final String OMNI_MARKER = "omni";
    private static final byte[] OMNI_MARKER_BYTES = OMNI_MARKER.getBytes(StandardCharsets.UTF_8);
    private static final int OMNI_MARKER_LENGTH = OMNI_MARKER.length();
    /** Maximum Class C Payload length (not counting the 4-byte {@code 'omni'} marker) */
    public static final int MAX_CLASS_C_PAYLOAD = 80 - OMNI_MARKER_LENGTH;

    /**
     * Construct an encoder
     */
    public ClassCEncoder() {
    }

    /**
     * Construct an encoder for a network
     * @param network The network this instance will encode transactions for
     * @deprecated Use {@link ClassCEncoder#ClassCEncoder()}
     */
    @Deprecated
    public ClassCEncoder(Network network) {
    }

    /**
     * Encode a bitcoinj {@code Transaction} with a reference output and an {@code OP_RETURN} output
     * @param refAddress Reference address (or {@code null})
     * @param payload Serialized Omni Payload
     * @return A bitcoinj {@code Transaction} object
     */
    public Transaction encode(Address refAddress, byte[] payload) {
        Transaction txClassC = new Transaction();

        TransactionOutput output = encodeOpReturnOutput(payload);
        txClassC.addOutput(output);

        // Add outputs to the transaction
        if (refAddress != null) {
            OmniTxBuilder.addDustOutput(txClassC, refAddress);                  // Add reference (aka destination) address output
        }

        return txClassC;
    }

    /**
     * @param payload Serialized Omni Payload
     * @return A bitcoinj {@code TransactionOutput} object containing the {@code OP_RETURN}
     */
    public TransactionOutput encodeOpReturnOutput(byte[] payload) {
        Script script = createOmniTxOpReturnScript(payload);

        return new TransactionOutput(null, Coin.ZERO, script.getProgram());
    }

    /**
     * @param payload Serialized Omni Payload
     * @return A bitcoinj {@code Script} object for the Class-C {@code OP_RETURN}
     */
    public static Script createOmniTxOpReturnScript(byte[] payload) {
        // Create OP_RETURN output with prefixedPayload
        return new ScriptBuilder()
                .op(ScriptOpCodes.OP_RETURN)
                .data(addOmniPrefix(payload))
                .build();
    }

    /**
     * @param payload Serialized Omni Payload without a prefix
     * @return new {@code byte[]} containing Serialized Omni Payload with a prefix
     */
    public static byte[] addOmniPrefix(byte[] payload) {
        byte[] prefixedPayload = new byte[payload.length + OMNI_MARKER_LENGTH];
        System.arraycopy(OMNI_MARKER_BYTES, 0, prefixedPayload, 0, OMNI_MARKER_LENGTH);
        System.arraycopy(payload, 0, prefixedPayload, OMNI_MARKER_LENGTH, payload.length);
        return prefixedPayload;
    }
}
