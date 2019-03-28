package foundation.omni.tx;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;

import java.util.List;

/**
 * Omni Protocol transaction
 *
 */
public class OmniTransaction extends Transaction {
    //Address fromAddress;
    //Address refAddress;
    //byte[] omniPayload;


    /* Create from Bitcoin transaction */
    public OmniTransaction(Transaction transaction) {
        super(transaction.getParams(), transaction.bitcoinSerialize());     // Is serialized byte array == payload byte array???

        // For now assume only a single multisig encoded output
        // Also Assume it is a simple send
        List<ECKey> keys = null;
        List<TransactionOutput> outputs = transaction.getOutputs();
        for (TransactionOutput output : outputs) {
            Script script = new Script(output.getScriptBytes());
            if (ScriptPattern.isSentToMultisig(script)) {
                keys = script.getPubKeys();
            }
        }
        if (keys == null) {
            throw new RuntimeException("Not an Omni Transaction");
        }

        // For debugging
        for (ECKey key : keys) {
            //System.out.println("key " + key);
            //System.out.println("address: " +  LegacyAddress.fromKey(transaction.getParams(), key));
        }

        /*
         * Convert the ECKeys back to a raw Omni transaction
         */

        // Note: we're assuming a simple send for now so only a single data key
        ECKey redeemKey = keys.get(0);
        ECKey dataKey = keys.get(1);    // 0th key is redeem key, 1st key contains data

        Address redeemAddress = LegacyAddress.fromKey(transaction.getParams(), redeemKey);

        byte[] input = dataKey.getPubKey();
        byte[] deobf = Obfuscation.obfuscate(input, redeemAddress);

        //System.out.println("deobf = " + RawTxBuilder.toHexString(deobf));

        // Create an Omni Transaction if valid otherwise throw "not Omni transaction"
        boolean valid = true;
        if (!valid) {
            throw new RuntimeException("Not an Omni Transaction");
        }
    }

    /* Create unsigned */
    public OmniTransaction(ECKey redeemingKey, Address refAddress, byte[] payload) throws InsufficientMoneyException {
        super(refAddress.getParameters());
        throw new RuntimeException("Not implemented yet");
    }

    /* Create Signed */
    public OmniTransaction(ECKey fromKey, List<TransactionOutput> unspentOutputs, Address refAddress, byte[] payload) throws InsufficientMoneyException {
        super(refAddress.getParameters());
        throw new RuntimeException("Not implemented yet");
    }

    /* Decode and/or return Omni payload as a stream of bytes */
    byte[] getOmniPayload() {
        return null;
    }

    /* Return the reference address */
    Address getReferenceAddress() {
        return null;
    }
}
