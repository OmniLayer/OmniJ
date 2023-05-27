package foundation.omni.tx;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Encode Data into MultiSig transaction outputs e.g. for Omni Protocol Class B transactions.</p>
 *
 * @author msgilligan
 * @author dexX7
 */
public class EncodeMultisig {

    private static final int maxKeys = 3;  /* Redeemable key + 2 data keys */
    private static final int maxDataKeys = maxKeys - 1;

    private final Network network;

    /**
     * Construct an encoder for a network
     * @param network network to encode for
     */
    public EncodeMultisig(Network network) {
        this.network = network;
    }

    /**
     * Encode data into transaction outputs
     *
     * @param redeemingKey key that can be used to redeem transaction output (1 of n multisig)
     * @param data Data bytes to encode into multisig output
     * @return Incomplete transaction with TransactionOutputs
     */
    public Transaction encode(ECKey redeemingKey, byte[] data) {
        List<ECKey> dataAsKeys = PubKeyConversion.convert(data);
        int numGroups = (dataAsKeys.size() + (maxDataKeys - 1)) / (maxDataKeys);

        // Create groups of keys, one group per multisig output
        List<List <ECKey>> keysByOutput = new ArrayList<>();
        for (int n = 0 ; n < numGroups ; n++) {
            int groupSize = Math.min(numGroups - n, maxDataKeys);
            int from = n * (maxDataKeys);
            int to = from + groupSize;
            List<ECKey> group = dataAsKeys.subList(from, to);
            keysByOutput.add(group);
        }

        Transaction txClassB = new Transaction(NetworkParameters.of(network));

        for (List<ECKey> group : keysByOutput) {
            // Add the redeemable key to the front of each group list
            List<ECKey> redeemableGroup = new ArrayList<>();
            redeemableGroup.add(redeemingKey);
            redeemableGroup.addAll(group);
            Script script = ScriptBuilder.createMultiSigOutputScript(1, redeemableGroup); // 1 of redeemableGroup.size() multisig
            TransactionOutput output = new TransactionOutput(NetworkParameters.of(network), null, Coin.ZERO, script.getProgram());
            output.setValue(output.getMinNonDustValue());    // TODO: Check this
            txClassB.addOutput(output);
        }

        return txClassB;
    }

    /**
     *
     * @param redeemingKey key that can be used to redeem transaction output (1 of n multisig)
     * @param data Data bytes to encode into multisig output
     * @param seed Seed string usually (always?) generated from a Bitcoin address
     * @return Incomplete transaction with TransactionOutputs
     */
    public Transaction encodeObfuscated(ECKey redeemingKey, byte[] data, String seed) {
        byte[] sequenced = SequenceNumbers.add(data);
        byte[] obf = Obfuscation.obfuscate(sequenced, seed);

        return encode(redeemingKey, obf);
    }
}
