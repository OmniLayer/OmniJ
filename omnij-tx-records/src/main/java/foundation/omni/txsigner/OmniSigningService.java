package foundation.omni.txsigner;

import foundation.omni.tx.OmniTxBuilder;
import foundation.omni.txrecords.TransactionParameters;
import org.bitcoinj.core.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A service to sign Omni transactions
 */
public class OmniSigningService {
    private final Object signingWalletKeyChain;
    private final OmniTxBuilder txBuilder;

    public OmniSigningService(NetworkParameters networkParameters, Object signingWalletKeyChain) {
        this.signingWalletKeyChain = signingWalletKeyChain;
        txBuilder = new OmniTxBuilder(networkParameters);
    }

    public CompletableFuture<Transaction> omniSignTx(List<TransactionOutput> utxos, Address fromAddress, TransactionParameters.OmniTx omniTx) throws InsufficientMoneyException {
        return omniSignTx(utxos, fromAddress, omniTx, fromAddress, Coin.ZERO);
    }

    public CompletableFuture<Transaction> omniSignTx(List<TransactionOutput> utxos, Address fromAddress, TransactionParameters.OmniTx omniTx, Address redeemAddress, Coin referenceAmount) throws InsufficientMoneyException {
        ECKey fromKey = (ECKey) null;  // Get signing key from signingWalletKeychain
        Address refAddress = (omniTx instanceof TransactionParameters.OmniRefTx refTx) ? refTx.referenceAddress() : null;
        Transaction tx = txBuilder.createSignedOmniTransaction(fromKey, /* fromAddress.getOutputScriptType(), */ utxos,  refAddress, omniTx.payload());
        return CompletableFuture.completedFuture(tx);
    }

}
