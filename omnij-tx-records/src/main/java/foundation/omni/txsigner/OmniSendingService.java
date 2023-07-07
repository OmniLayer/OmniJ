package foundation.omni.txsigner;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.txrecords.TransactionRecords;
import foundation.omni.txrecords.UnsignedTxSimpleSend;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoinj.signing.TransactionInputData;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A service to gather inputs for, sign, and send a transaction. Typically uses {@link OmniSigningService} for
 * signing.
 */
public interface OmniSendingService {
    /**
     * Find inputs, Sign and Send an Omni Simple Send transaction
     * <p>
     * This method is compatible with the Omni Core {@code omni_send} JSON-RPC method and if made available from a JSON-RPC server should work equivalently.
     * (The last two, optional arguments are not currently implemented.)
     *
     * @param fromAddress Omni address sending funds
     * @param toAddress Omni address receiving funds
     * @param currency Currency type
     * @param amount amount
     * @return A future for the transaction hash
     * @throws IOException A failure occurred fetching inputs or broadcasting the transaction
     */
    CompletableFuture<Sha256Hash> omniSend(Address fromAddress, Address toAddress, CurrencyID currency, OmniValue amount) throws IOException;

    /**
     * Sign and Send an Omni Simple Send transaction. <b>Incubating</b>.
     *
     * @param simpleSend An object holding all the parameters (including UTXO inputs) for a simple send transaction
     * @return A future for the transaction hash
     * @throws IOException A failure occurred fetching broadcasting the transaction
     */
    CompletableFuture<Sha256Hash> omniSend(UnsignedTxSimpleSend simpleSend) throws IOException;

    /**
     * Build a complete, unsigned SimpleSend ("signing request") transaction.
     * Fetches the transaction inputs, assembles the payload, etc.
     *
     * @param fromAddress Omni address sending funds
     * @param toAddress Omni address receiving funds
     * @param currency Currency type
     * @param amount amount
     * @return A record containing all necessary data for signing
     * @throws IOException if an error occurred fetching inputs
     */
    default UnsignedTxSimpleSend assembleSimpleSend(Address fromAddress, Address toAddress, CurrencyID currency, OmniValue amount) throws IOException {
        List<TransactionInputData> utxos = getInputsFor(fromAddress);
        TransactionRecords.SimpleSend sendTx = new TransactionRecords.SimpleSend(toAddress, currency, amount);
        return new UnsignedTxSimpleSend(fromAddress, utxos, sendTx, toAddress);
    }


    List<TransactionInputData> getInputsFor(Address fromAddress) throws IOException;

    CompletableFuture<Sha256Hash> sendRawTransactionAsync(Transaction tx);
}
