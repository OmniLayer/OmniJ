package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.net.OmniNetwork;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;

import java.util.Collection;
import java.util.List;

/**
 * Builds Omni transactions in bitcoinj Transaction objects
 */
public class OmniTxBuilder {
    private final Network network;
    private final OmniNetwork omniNetwork;
    private final RawTxBuilder builder = new RawTxBuilder();
    private final EncodeMultisig transactionEncoder;
    private final ClassCEncoder classCEncoder;
    private final FeeCalculator feeCalculator;


    public OmniTxBuilder(BitcoinNetwork bitcoinNetwork) {
        this(OmniNetwork.of(bitcoinNetwork));
    }

    public OmniTxBuilder(OmniNetwork omniNetwork) {
        this(omniNetwork, new DefaultFixedFeeCalculator());
    }

    public OmniTxBuilder(BitcoinNetwork bitcoinNetwork, FeeCalculator feeCalculator) {
        this(OmniNetwork.of(bitcoinNetwork), feeCalculator);
    }

    /**
     * @param omniNetwork The network to construct transactions for
     * @param feeCalculator transaction fee calculator
     */
    public OmniTxBuilder(OmniNetwork omniNetwork, FeeCalculator feeCalculator) {
        this.network = omniNetwork.bitcoinNetwork();
        this.omniNetwork = omniNetwork;
        this.transactionEncoder = new EncodeMultisig(network);
        this.classCEncoder = new ClassCEncoder();
        this.feeCalculator = feeCalculator;
    }

    /**
     * <p>Create unsigned Omni transaction (P2PKH) in a bitcoinj Transaction object</p>
     *
     * <p>TODO: Exact output amounts.</p>
     *
     * @param redeemingKey Public key used for creating redeemable multisig data outputs (for Class B)
     * @param refAddress (optional) Omni reference address (for the reference output) or null
     * @param payload Omni transaction payload as a raw byte array
     * @return Incomplete Transaction, no inputs or change output
     */
    public Transaction createOmniTransaction(ECKey redeemingKey, Address refAddress, byte[] payload) {
        if (payload.length < ClassCEncoder.MAX_CLASS_C_PAYLOAD) {
            return createClassCTransaction(refAddress, payload);
        } else {
            return createClassBTransaction(redeemingKey, ScriptType.P2PKH, refAddress, payload);
        }
    }

    /**
     * Create unsigned Class C Omni transaction in a bitcoinj Transaction object
     *
     * @param refAddress (optional) Omni reference address (for the reference output) or null
     * @param payload Omni transaction payload as a raw byte array
     * @return Incomplete Transaction, no inputs or change output
     */
    public Transaction createClassCTransaction(Address refAddress, byte[] payload) {
        // Encode the Omni Protocol Payload as a Class C transaction
        Transaction tx = classCEncoder.encode(refAddress, payload);

        // Add outputs to the transaction
        if (refAddress != null) {
            addDustOutput(tx, refAddress);                  // Add reference (aka destination) address output
        }
        return tx;
    }

    /**
     * Create unsigned Class B Omni transaction in a bitcoinj Transaction object
     *
     * @param redeemingKey Public key used for creating redeemable multisig data outputs
     * @param scriptType script type for redeemingKey address - P2PKH or other single-key script type
     * @param refAddress (optional) Omni reference address (for the reference output) or null
     * @param payload Omni transaction payload as a raw byte array
     * @return Incomplete Transaction, no inputs or change output
     */
    public Transaction createClassBTransaction(ECKey redeemingKey, ScriptType scriptType, Address refAddress, byte[] payload) {
        Address redeemingAddress = redeemingKey.toAddress(scriptType, network);

        // Encode the Omni Protocol Payload as a Class B transaction
        Transaction tx = transactionEncoder.encodeObfuscated(redeemingKey, payload, redeemingAddress.toString());

        // Add outputs to the transaction
        addDustOutput(tx, omniNetwork.exodusAddress());   // Add Exodus Output for this chain
        if (refAddress != null) {
            addDustOutput(tx, refAddress);                  // Add reference (aka destination) address output
        }
        return tx;
    }

    /**
     * Add a transaction output with the minimum non-dust output value
     * @param tx Parent transaction to add the output to
     * @param address destination address
     */
    static void addDustOutput(Transaction tx, Address address) {
        TransactionOutput output = tx.addOutput(Coin.ZERO, address);    // Add Output with zero amount
        output.setValue(output.getMinNonDustValue());                   // Adjust to minimum non-dust value
    }

    /**
     * Create a signed Omni Class B (from a single P2PKH address) transaction in a bitcoinj Transaction object
     *
     * @param fromKey Private key/address to send from and receive change to
     * @param unspentOutputs A collection of unspent outputs for funding the transaction
     * @param refAddress The Omni reference address (for the reference output)
     * @param payload Omni transaction payload as a raw byte array
     * @throws InsufficientMoneyException Not enough bitcoin for fees
     * @return Signed and ready-to-send Transaction
     */
    public Transaction createSignedOmniTransaction(ECKey fromKey, List<TransactionOutput> unspentOutputs, Address refAddress, byte[] payload)
            throws InsufficientMoneyException {
        return createSignedClassBTransaction(fromKey, ScriptType.P2PKH, unspentOutputs, refAddress, payload);
    }
    
    /**
     * Create a signed Omni Class B (from a single address) transaction in a bitcoinj Transaction object
     *
     * @param fromKey Private key/address to send from and receive change to
     * @param scriptType Script Type to use (alternatively we could take an address??)
     * @param unspentOutputs A collection of unspent outputs for funding the transaction
     * @param refAddress The Omni reference address (for the reference output)
     * @param payload Omni transaction payload as a raw byte array
     * @throws InsufficientMoneyException Not enough bitcoin for fees
     * @return Signed and ready-to-send Transaction
     * @throws InsufficientMoneyException if unspentOutputs contain insufficient funds for the transaction
     */
    public Transaction createSignedClassBTransaction(ECKey fromKey, ScriptType scriptType, Collection<TransactionOutput> unspentOutputs, Address refAddress, byte[] payload)
            throws InsufficientMoneyException {
        Address fromAddress = fromKey.toAddress(scriptType, network);

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
            // ScriptPattern should match scriptType or error
            if (ScriptPattern.isP2PK(scriptPubKey))
                input.setScriptSig(ScriptBuilder.createInputScript(signature));
            else if (ScriptPattern.isP2PKH(scriptPubKey))
                input.setScriptSig(ScriptBuilder.createInputScript(signature, fromKey));
            else if (ScriptPattern.isP2SH(scriptPubKey))
                // TODO: Support signing P2SH(P2WPKH) here
                // (since this method takes a single ECKey we don't need multisig support in this method)
                throw new RuntimeException("Don't know how to sign for P2SH scriptPubKey yet: " + scriptPubKey);
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
    public Transaction createUnsignedOmniTransaction(ECKey fromKey, List<TransactionInput> inputs, Address refAddress, byte[] payload)
            throws InsufficientMoneyException {
        Address fromAddress = fromKey.toAddress(ScriptType.P2PKH, network);

        Transaction tx = createOmniTransaction(fromKey, refAddress, payload);

        for (TransactionInput input : inputs) {
            tx.addInput(input);
        }

        makeChangeOutput(tx, fromAddress, sumInputs(inputs));   // Calculate change and, if any, return to the fromAddress

        return tx;
    }

    /**
     * Create a signed Class B (from a single P2PKH address) simple send Transaction
     *
     * @param fromKey Private key (P2PKH address) to send from
     * @param unspentOutputs A collection of unspent outputs for funding the transaction
     * @param toAddress The Omni reference address (for the reference output, destination address in this case)
     * @param currencyID The Omni currency ID
     * @param amount The currency amount in willetts
     * @throws InsufficientMoneyException Not enough bitcoin for fees
     * @return Signed and ready-to-send Transaction
     */
    public Transaction createSignedSimpleSend(ECKey fromKey, List<TransactionOutput> unspentOutputs, Address toAddress, CurrencyID currencyID, OmniValue amount)
            throws InsufficientMoneyException {
        byte[] payload = builder.createSimpleSend(currencyID, amount);
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
    public Transaction createUnsignedSimpleSend(ECKey fromKey, List<TransactionInput> inputs, Address toAddress, CurrencyID currencyID, OmniValue amount)
            throws InsufficientMoneyException {
        byte[] payload = builder.createSimpleSend(currencyID, amount);
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
        return outputs.stream()
                .mapToLong(output -> output.getValue().toSat())
                .sum();
    }

    /**
     * Calculate the total value of a collection of transaction inputs.
     *
     * @param inputs list of transaction outputs to total
     * @return total value in satoshis
     */
    private long sumInputs(Collection<TransactionInput> inputs) {
        return inputs.stream()
                .mapToLong(input -> input.getValue().toSat())
                .sum();
    }
}
