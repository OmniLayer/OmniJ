package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.net.OmniNetworkParameters;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;

import java.util.List;

/**
 * Builds Omni transactions in bitcoinj Transaction objects
 *
 */
public class OmniTxBuilder {
    private final long stdTxFee = 10000;
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
        tx.addOutput(Coin.MILLICOIN, omniParams.getExodusAddress());    // Add correct Exodus Output
        if (refAddress != null) {
            tx.addOutput(Coin.CENT, refAddress);                            // Reference (destination) address output
        }
        return tx;
    }

    /**
     * <p>Create a signed Omni transaction in a bitcoinj Transaction object</p>
     *
     *
     * @param fromKey Private key/address to send from and receive change to
     * @param unspentOutputs A list of unspent outputs for funding the transaction
     * @param refAddress The Omni reference address (for the reference output)
     * @param payload Omni transaction payload as a raw byte array
     * @return Signed and ready-to-send Transaction
     */
    public Transaction createSignedOmniTransaction(ECKey fromKey, List<TransactionOutput> unspentOutputs, Address refAddress, byte[] payload) {
        Address fromAddress = fromKey.toAddress(netParams);

        Transaction tx = createOmniTransaction(fromKey, refAddress, payload);

        makeChangeOutput(tx, fromAddress, unspentOutputs);  // Return change to the fromAddress

        // Add all UTXOs for fromAddress as signed inputs
        for (TransactionOutput output : unspentOutputs) {
            tx.addSignedInput(output, fromKey);
        }

        return tx;
    }

    /**
     * Create a signed simple send Transaction
     *
     * @param fromKey Private key/address to send from
     * @param unspentOutputs A list of unspent outputs for funding the transaction
     * @param toAddress The Omni reference address (for the reference output, destination address in this case)
     * @param currencyID The Omni currency ID
     * @param amount The currency amount in willets
     * @return Signed and ready-to-send Transaction
     */
    public Transaction createSignedSimpleSend(ECKey fromKey, List<TransactionOutput> unspentOutputs, Address toAddress, CurrencyID currencyID, long amount) {
        String txHex = builder.createSimpleSendHex(currencyID, amount);
        byte[] payload = RawTxBuilder.hexToBinary(txHex);
        return createSignedOmniTransaction(fromKey, unspentOutputs, toAddress, payload);
    }

    /**
     * <p>Calculate change and create a change output</p>
     *
     * <p>TODO: Calculate fee dynamically.</p>
     *
     * @param tx Transaction with all non-change outputs attached
     * @param changeAddress Address to receive the change
     * @param unspentOutputs Unspent outputs for use in calculating change
     * @return The modified transaction, still needs signed inputs
     */
    Transaction makeChangeOutput(Transaction tx, Address changeAddress, List<TransactionOutput> unspentOutputs) {
        // Calculate change
        long amountIn     = sum(unspentOutputs);    // Sum of available UTXOs
        long amountOut    = sum(tx.getOutputs());   // Sum of outputs, this transaction
        long amountChange = amountIn - amountOut - stdTxFee;
        // If change is negative, transaction is invalid
        if (amountChange < 0) {
            // TODO: Throw Exception
            System.out.println("Insufficient funds");
        }
        // If change is positive, return it all to the sending address
        if (amountChange > 0) {
            // Add a change output
            tx.addOutput(Coin.valueOf(amountChange), changeAddress);
        }
        return tx;
    }

    /**
     * Calculate the total value of a list of transaction outputs.
     *
     * @param outputs list of transaction outputs to total
     * @return total value in satoshis
     */
    long sum(List <TransactionOutput> outputs) {
        long sum = 0;
        for (TransactionOutput output : outputs) {
            sum += output.getValue().longValue();
        }
        return sum;
    }
}
