package foundation.omni.txsigner;

import foundation.omni.txrecords.UnsignedTxSimpleSend;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoinj.signing.DefaultSigningRequest;
import org.consensusj.bitcoinj.signing.FeeCalculator;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.consensusj.bitcoinj.signing.SigningUtils;
import org.consensusj.bitcoinj.signing.HDKeychainSigner;
import org.consensusj.bitcoinj.signing.TransactionInputData;
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static foundation.omni.tx.Transactions.OmniTx;
import static foundation.omni.tx.Transactions.OmniRefTx;

/**
 * A service to sign Omni transactions
 */
public class OmniKeychainSigningService implements OmniSigningService {
    private final NetworkParameters netParams;
    private final BipStandardDeterministicKeyChain keyChain;
    private final HDKeychainSigner signingWalletKeyChain;
    private final FeeCalculator feeCalculator;


    public OmniKeychainSigningService(NetworkParameters netParams, BipStandardDeterministicKeyChain deterministicKeyChain) {
        this.netParams = netParams;
        this.keyChain = deterministicKeyChain;
        this.signingWalletKeyChain = new HDKeychainSigner(deterministicKeyChain);
        feeCalculator = new HackedFeeCalculator();
    }

    /* package */ BipStandardDeterministicKeyChain getKeychain() {
        return keyChain;
    }

//    public CompletableFuture<Transaction> omniSignTx(List<TransactionOutput> utxos, Address fromAddress, OmniTx omniTx) throws InsufficientMoneyException {
//        return omniSignTx(utxos, fromAddress, omniTx, fromAddress, Coin.ZERO);
//    }

//    public CompletableFuture<Transaction> omniSignTx(List<TransactionOutput> utxos, Address fromAddress, OmniTx omniTx, Address redeemAddress, Coin referenceAmount) throws InsufficientMoneyException {
//        ECKey fromKey = (ECKey) null;  // Get signing key from signingWalletKeychain
//        Address refAddress = (omniTx instanceof OmniRefTx refTx) ? refTx.referenceAddress() : null;
//        Transaction tx = txBuilder.createSignedOmniTransaction(fromKey, /* fromAddress.getOutputScriptType(), */ utxos,  refAddress, omniTx.payload());
//        return CompletableFuture.completedFuture(tx);
//    }



    /**
     * Sign a Bitcoin transaction (possibly with an embedded Omni Class C or Class B transaction)
     *
     * @param signingRequest a ConsensusJ signing request
     * @return a signed bitcoinj transaction
     */
    public CompletableFuture<Transaction> signTx(SigningRequest signingRequest) {
        return signingWalletKeyChain.signTransaction(signingRequest);
    }

    @Override
    public FeeCalculator feeCalculator() {
        return feeCalculator;
    }

    @Override
    public NetworkParameters netParams() {
        return netParams;
    }
}
