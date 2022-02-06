package foundation.omni.txsigner;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.txrecords.TransactionParameters;
import org.bitcoinj.core.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A service to sign and send Omni Transactions (similar to functionality in Omni Core)
 * TODO: To support an equivalent API to the Omni Core send functions, the ability to find
 * UTXOs for specified addresses is needed.
 * and to send transactions we need either a P2P client (e.g. PeerGroup) or server API.
 */
public class OmniSendService {
    private final OmniSigningService signingService;

    public OmniSendService(OmniSigningService signingService) {
        this.signingService = signingService;
    }

    public CompletableFuture<Sha256Hash> omniSend(Address fromAddress, Address toAddress, CurrencyID currency, OmniValue amount) throws InsufficientMoneyException {
        // TODO: We can't do this without a blockchain or network access to one.
        // Note: the `getaddressutxos` RPC method (Omni/BitPay) should do the trick, but we need network access for this
        List<TransactionOutput> utxos = List.of();
        TransactionParameters.SimpleSend sendTx = new TransactionParameters.SimpleSend(toAddress, currency, amount);
        CompletableFuture<Transaction> signedTx = signingService.omniSignTx(utxos, fromAddress, sendTx);
        // TODO: Send the transaction
        return signedTx.thenApply(Transaction::getTxId);
    }
}
