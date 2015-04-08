package foundation.omni.tx;

import foundation.omni.CurrencyID;
import foundation.omni.net.OmniNetworkParameters;
import foundation.omni.net.OmniRegTestParams;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.RegTestParams;

import java.util.List;

/**
 * Builds signed Omni transactions in bitcoinj Transaction objects
 *
 * TODO: ability to create unsigned transactions
 *
 */
public class OmniTxBuilder {
    private final long stdTxFee = 10_000;
    private final NetworkParameters netParams;
    private final OmniNetworkParameters omniParams;
    private final RawTxBuilder builder = new RawTxBuilder();

    public OmniTxBuilder(NetworkParameters netParams) {
        this.netParams = netParams;
        this.omniParams = OmniNetworkParameters.fromBitcoinParms(netParams);
    }

    public Transaction createSignedSimpleSend(ECKey fromKey, List<TransactionOutput> unspentOutputs, Address toAddress, CurrencyID currencyID, long amount) {
        String txHex = builder.createSimpleSendHex(currencyID, amount);
        byte[] payload = RawTxBuilder.hexToBinary(txHex);
        return createSignedOmniTransaction(fromKey, unspentOutputs, toAddress, payload);
    }

    /**
     * Create a signed Omni transaction in a bitcoinj Transaction object
     *
     * @param fromKey Private key/address to send from
     * @param unspentOutputs A list of unspent outputs for funding the transaction
     * @param refAddress The Omni reference address (for the reference output)
     * @param payload Omni transaction payload as a raw byte array
     * @return Signed and ready-to-send Transaction
     */
    public Transaction createSignedOmniTransaction(ECKey fromKey, List<TransactionOutput> unspentOutputs, Address refAddress, byte[] payload) {
        Address fromAddress = fromKey.toAddress(netParams);

        // Encode the Omni Protocol Payload as a Class B transaction
        Transaction tx = EncodeMultisig.encodeObfuscated(fromKey, payload, fromAddress.toString());

        // Add outputs to the transaction
        tx.addOutput(Coin.MILLICOIN, omniParams.getExodusAddress());    // Add correct Exodus Output
        tx.addOutput(Coin.CENT, refAddress);                            // Reference (destination) address output

        // Calculate change
        long amountIn     = sum(unspentOutputs);    // Sum of available UTXOs
        long amountOut    = sum(tx.getOutputs());   // Sum of outputs, this transaction
        long amountChange = amountIn - amountOut - stdTxFee;
        // If change is negative, transaction is invalid
        if (amountChange < 0) {
            // TODO: Throw Exception
            System.out.println("Insufficient funds");
        }
        // If change is positive, return it all the the sending address
        if (amountChange > 0) {
            // Add a change output
            tx.addOutput(Coin.valueOf(amountChange), fromAddress);
        }

        // Add all UTXOs for fromAddress as inputs
        for (TransactionOutput output : unspentOutputs) {
            tx.addSignedInput(output, fromKey);
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
