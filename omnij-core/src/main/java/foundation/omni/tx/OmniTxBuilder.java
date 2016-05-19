package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.net.OmniNetworkParameters;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;

import java.util.Collection;

/**
 * Builds Omni transactions in bitcoinj Transaction objects
 */
public class OmniTxBuilder {
    private final NetworkParameters netParams;
    private final OmniNetworkParameters omniParams;
    private final RawTxBuilder builder = new RawTxBuilder();
    private final EncodeMultisig transactionEncoder;

    /**
     * @param netParams The Bitcoin network to construct transactions for
     */
    public OmniTxBuilder(NetworkParameters netParams) {
        this.netParams = netParams;
        this.omniParams = OmniNetworkParameters.fromBitcoinParms(netParams);
        this.transactionEncoder = new EncodeMultisig(netParams);
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
        Address redeemingAddress = redeemingKey.toAddress(netParams);

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
     * @return Signed and ready-to-send Transaction
     */
    public Transaction createSignedOmniTransaction(ECKey fromKey, Collection<TransactionOutput> unspentOutputs, Address refAddress, byte[] payload)
            throws InsufficientMoneyException {
        Address fromAddress = fromKey.toAddress(netParams);

        Transaction tx = createOmniTransaction(fromKey, refAddress, payload);

        makeChangeOutput(tx, fromAddress, sum(unspentOutputs));  // Return change to the fromAddress

        // Add all UTXOs for fromAddress as signed inputs
        for (TransactionOutput output : unspentOutputs) {
            tx.addSignedInput(output, fromKey);
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
     * @throws InsufficientMoneyException
     */
    public Transaction createUnsignedOmniTransaction(ECKey fromKey, Collection<TransactionInput> inputs, Address refAddress, byte[] payload)
            throws InsufficientMoneyException {
        Address fromAddress = fromKey.toAddress(netParams);

        Transaction tx = createOmniTransaction(fromKey, refAddress, payload);

        makeChangeOutput(tx, fromAddress, sumInputs(inputs));

        for (TransactionInput input : inputs) {
            tx.addInput(input);
        }

        return tx;
    }

    /**
     * Create a signed simple send Transaction
     *
     * @param fromKey Private key/address to send from
     * @param unspentOutputs A collection of unspent outputs for funding the transaction
     * @param toAddress The Omni reference address (for the reference output, destination address in this case)
     * @param currencyID The Omni currency ID
     * @param amount The currency amount in willets
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
     * @param amount The currency amount in willets
     * @return unsigned transaction
     * @throws InsufficientMoneyException
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
     * @throws InsufficientMoneyException
     */
    private Transaction makeChangeOutput(Transaction tx, Address changeAddress, long totalInputAmount) throws InsufficientMoneyException {
        long amountOut    = sum(tx.getOutputs());   // Sum of outputs, this transaction
        long amountChange = totalInputAmount - amountOut - Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.value;

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
