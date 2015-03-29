package foundation.omni.tx;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * WIP
 */
public class EncodeMultisig {

    private static final int maxKeys = 3;
    private static final int dustAmountInSatoshis = 100_000;

    /**
     * Encode data into transaction outputs
     *
     * @param redeemingKey
     * @param data
     * @return Incomplete transaction with TransactionOutputs
     */
    public static Transaction encode(ECKey redeemingKey, byte[] data) {
        List<TransactionOutput> outputs = null;
        List<ECKey> dataAsKeys = PubKeyConversion.convert(data);
        int numGroups = (dataAsKeys.size() + (maxKeys - 1)) / maxKeys;

        // Create groups of keys, one group per multisig output
        List<List <ECKey>> keysByOutput = new ArrayList<List <ECKey>>();
        for (int n = 0 ; n < numGroups ; n++) {
            int groupSize = Math.min(numGroups - n, maxKeys);
            int from = n * maxKeys;
            int to = from + groupSize;
            List<ECKey> group = dataAsKeys.subList(from, to);
            keysByOutput.add(group);
        }

        Transaction classBTx = new Transaction(RegTestParams.get()); // Hard code regtest for now

        for (List<ECKey> group : keysByOutput) {
            // Add the redeemable key to the front of each group list
            List<ECKey> redeemableGroup = new ArrayList<ECKey>();
            redeemableGroup.add(redeemingKey);
            redeemableGroup.addAll(group);
            Script script = ScriptBuilder.createMultiSigOutputScript(1, redeemableGroup); // 1 of group.size() multisig
            Coin amount = Coin.valueOf(dustAmountInSatoshis); // TODO: Make this dynamic
            classBTx.addOutput(amount, script);
        }

        return classBTx;
    }

    public static Transaction encodeObfuscated(ECKey redeemingKey, byte[] data, String seed) {
        byte[] sequenced = SequenceNumbers.add(data);
        byte[] obf = Obfuscation.obfuscate(sequenced, seed);

        return encode(redeemingKey, obf);
    }
}
