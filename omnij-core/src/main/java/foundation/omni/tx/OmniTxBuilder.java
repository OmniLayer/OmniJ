package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.net.OmniNetworkParameters;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;

import java.util.Collection;

/**
 * Builds Omni transactions in bitcoinj Transaction objects
 */
public class OmniTxBuilder {
    private final NetworkParameters netParams;
    private final OmniNetworkParameters omniParams;
    private final RawTxBuilder builder = new RawTxBuilder();
    private final EncodeMultisig transactionEncoder;
    private final FeeCalculator feeCalculator;

    /**
     * @param netParams The Bitcoin network to construct transactions for
     */
    public OmniTxBuilder(NetworkParameters netParams) {
        this(netParams, new DefaultFixedFeeCalculator());
    }

    public OmniTxBuilder(NetworkParameters netParams, FeeCalculator feeCalculator) {
        this.netParams = netParams;
        this.omniParams = OmniNetworkParameters.fromBitcoinParms(netParams);
        this.transactionEncoder = new EncodeMultisig(netParams);
        this.feeCalculator = feeCalculator;
    }

    /**
     * <p>Create unsigned Omni transaction in a bitcoinj Transaction object</p>
     *
     * <p>TODO: Exact output amounts.</p>
     *
     * @param redeemingKey Public key used for creating redeemable multisig data outputs
     * @param refAddress (optional) Omni reference address (for the reference output) or null
     * @param payload Omni transaction payload as a raw byte array
     * @return Incomplete Transaction, no inputs or change output
     */
    public Transaction createOmniTransaction(ECKey redeemingKey, Address refAddress, byte[] payload) {
        Address redeemingAddress = Address.fromKey(netParams, redeemingKey, Script.ScriptType.P2PKH);

        // Encode the Omni Protocol Payload as a Class B transaction
        Transaction tx = transactionEncoder.encodeObfuscated(redeemingKey, payload, redeemingAddress.toString());

        // Add outputs to the transaction
        tx.addOutput(Transaction.MIN_NONDUST_OUTPUT, omniParams.getExodusAddress());    // Add correct Exodus Output
        if (refAddress != null) {
            tx.addOutput(Transaction.MIN_NONDUST_OUTPUT, refAddress);                   // Reference (destination) address output
        }
        return tx;
    }

    /**
     * Create a signed Omni transaction in a bitcoinj Transaction object
     *
     * @param fromKey Private key/address to send from and receive change to
     * @param unspentOutputs A collection of unspent outputs for funding the transaction
     * @param refAddress The Omni reference address (for the reference output)
     * @param payload Omni transaction payload as a raw byte array
     * @throws InsufficientMoneyException Not enough bitcoin for fees
     * @return Signed and ready-to-send Transaction
     */
    public Transaction createSignedOmniTransaction(ECKey fromKey, Collection<TransactionOutput> unspentOutputs, Address refAddress, byte[] payload)
            throws InsufficientMoneyException {
        Address fromAddress = Address.fromKey(netParams, fromKey, Script.ScriptType.P2PKH);

        Transaction tx = createOmniTransaction(fromKey, refAddress, payload);

        // Add all UTXOs for fromAddress as unsigned inputs, so fee calculator can use length
        for (TransactionOutput output : unspentOutputs) {
            tx.addInput(output);
        }

        makeChangeOutput(tx, fromAddress, sum(unspentOutputs));  // Calculate fees/change and, if any, return to the fromAddress

        // Sign the transaction inputs
        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput input = tx.getInput(i);
            Script scriptPubKey = input.getConnectedOutput().getScriptPubKey();
            TransactionSignature signature = tx.calculateSignature(i, fromKey, scriptPubKey, Transaction.SigHash.ALL, false);
            if (ScriptPattern.isP2PK(scriptPubKey))
                input.setScriptSig(ScriptBuilder.createInputScript(signature));
            else if (ScriptPattern.isP2PKH(scriptPubKey))
                input.setScriptSig(ScriptBuilder.createInputScript(signature, fromKey));
            else
                throw new RuntimeException("Don't know how to sign for this kind of scriptPubKey: " + scriptPubKey);

        }
        return tx;
    }

    /**
     * Create an unsigned Omni transaction, with unsigned inputs in a bitcoinj Transaction object
     *
     * @param fromKey Private key/address to send from and receive change to
     * @param inputs - a collection of inputs to add to the transaction
     * @param refAddress The Omni reference address (for the reference output)
     * @param payload Omni transaction payload as a raw byte array
     * @return Unsigned OmniTransaction Transaction
     * @throws InsufficientMoneyException Not enough bitcoin for fees
     */
    public Transaction createUnsignedOmniTransaction(ECKey fromKey, Collection<TransactionInput> inputs, Address refAddress, byte[] payload)
            throws InsufficientMoneyException {
        Address fromAddress = Address.fromKey(netParams, fromKey, Script.ScriptType.P2PKH);

        Transaction tx = createOmniTransaction(fromKey, refAddress, payload);

        for (TransactionInput input : inputs) {
            tx.addInput(input);
        }

        makeChangeOutput(tx, fromAddress, sumInputs(inputs));   // Calculate change and, if any, return to the fromAddress

        return tx;
    }

    /**
     * Create a signed simple send Transaction
     *
     * @param fromKey Private key/address to send from
     * @param unspentOutputs A collection of unspent outputs for funding the transaction
     * @param toAddress The Omni reference address (for the reference output, destination address in this case)
     * @param currencyID The Omni currency ID
     * @param amount The currency amount in willetts
     * @throws InsufficientMoneyException Not enough bitcoin for fees
     * @return Signed and ready-to-send Transaction
     */
    public Transaction createSignedSimpleSend(ECKey fromKey, Collection<TransactionOutput> unspentOutputs, Address toAddress, CurrencyID currencyID, OmniValue amount)
            throws InsufficientMoneyException {
        String txHex = builder.createSimpleSendHex(currencyID, amount);
        byte[] payload = RawTxBuilder.hexToBinary(txHex);
        return createSignedOmniTransaction(fromKey, unspentOutputs, toAddress, payload);
    }

    /**
     * Create a signed simple send Transaction
     *
     * @param fromKey Private key/address to send from
     * @param inputs unsigned inputs to use for the transaction
     * @param toAddress The Omni reference address (for the reference output, destination address in this case)
     * @param currencyID The Omni currency ID
     * @param amount The currency amount in willetts
     * @return unsigned transaction
     * @throws InsufficientMoneyException Not enough bitcoin for fees
     */
    public Transaction createUnsignedSimpleSend(ECKey fromKey, Collection<TransactionInput> inputs, Address toAddress, CurrencyID currencyID, OmniValue amount)
            throws InsufficientMoneyException {
        String txHex = builder.createSimpleSendHex(currencyID, amount);
        byte[] payload = RawTxBuilder.hexToBinary(txHex);
        return createUnsignedOmniTransaction(fromKey, inputs, toAddress, payload);
    }

    /**
     * Calculate change amount and add a change output to transaction
     * <p>TODO: Calculate fee dynamically.</p>
     *
     * @param tx transaction to add output to
     * @param changeAddress address for output
     * @param totalInputAmount total of inputs (in satoshis)
     * @return The modified transaction
     * @throws InsufficientMoneyException Not enough bitcoin for fees
     */
    private Transaction makeChangeOutput(Transaction tx, Address changeAddress, long totalInputAmount) throws InsufficientMoneyException {
        long amountOut    = sum(tx.getOutputs());   // Sum of outputs, this transaction
        long fee = feeCalculator.calculateFee(tx).getValue();
        long amountChange = totalInputAmount - amountOut - fee;

        // If change is negative, transaction is invalid
        if (amountChange < 0) {
            Coin missing = Coin.valueOf(-amountChange);
            throw new InsufficientMoneyException(missing, "Insufficient Bitcoin to build Omni Transaction");
        }
        // If change is positive, return it all to the sending address
        if (amountChange > 0) {
            // Add a change output
            tx.addOutput(Coin.valueOf(amountChange), changeAddress);
        }
        return tx;
    }

    /**
     * Calculate the total value of a collection of transaction outputs.
     *
     * @param outputs list of transaction outputs to total
     * @return total value in satoshis
     */
    private long sum(Collection<TransactionOutput> outputs) {
        long sum = 0;
        for (TransactionOutput output : outputs) {
            sum += output.getValue().value;
        }
        return sum;
    }

    /**
     * Calculate the total value of a collection of transaction inputs.
     *
     * @param inputs list of transaction outputs to total
     * @return total value in satoshis
     */
    private long sumInputs(Collection<TransactionInput> inputs) {
        long sum = 0;
        for (TransactionInput input : inputs) {
            sum += input.getValue().value;
        }
        return sum;
    }
}
