package foundation.omni.tx;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

import java.nio.charset.StandardCharsets;

/**
 *
 */
public class ClassCEncoder {
    private static final String OMNI_MARKER = "omni";
    private static final byte[] OMNI_MARKER_BYTES = OMNI_MARKER.getBytes(StandardCharsets.UTF_8);
    private static final int OMNI_MARKER_LENGTH = OMNI_MARKER.length();
    public static final int MAX_CLASS_C_PAYLOAD = 80 - OMNI_MARKER_LENGTH;
    private final NetworkParameters netParams;

    public ClassCEncoder(NetworkParameters netParams) {
        this.netParams = netParams;
    }

    public Transaction encode(Address refAddress, byte[] payload) {
        Transaction txClassC = new Transaction(netParams);

        TransactionOutput output = encodeOpReturnOutput(payload);
        txClassC.addOutput(output);

        // Add outputs to the transaction
        if (refAddress != null) {
            OmniTxBuilder.addDustOutput(txClassC, refAddress);                  // Add reference (aka destination) address output
        }

        return txClassC;
    }

    public TransactionOutput encodeOpReturnOutput(byte[] payload) {
        Script script = createOmniTxOpReturnScript(payload);

        return new TransactionOutput(netParams, null, Coin.ZERO, script.getProgram());
    }

    public static Script createOmniTxOpReturnScript(byte[] payload) {
        // Create OP_RETURN output with prefixedPayload
        return new ScriptBuilder()
                .op(ScriptOpCodes.OP_RETURN)
                .data(addOmniPrefix(payload))
                .build();
    }

    public static byte[] addOmniPrefix(byte[] payload) {
        byte[] prefixedPayload = new byte[payload.length + OMNI_MARKER_LENGTH];
        System.arraycopy(OMNI_MARKER_BYTES, 0, prefixedPayload, 0, OMNI_MARKER_LENGTH);
        System.arraycopy(payload, 0, prefixedPayload, OMNI_MARKER_LENGTH, payload.length);
        return prefixedPayload;
    }
}
