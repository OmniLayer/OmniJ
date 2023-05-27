package foundation.omni.txsigner;

import org.bitcoinj.base.Network;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.consensusj.bitcoinj.signing.FeeCalculator;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.consensusj.bitcoinj.signing.HDKeychainSigner;
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain;

import java.util.concurrent.CompletableFuture;


/**
 * A service to sign Omni transactions
 */
public class OmniKeychainSigningService implements OmniSigningService {
    private final Network network;
    private final DeterministicKeyChain keyChain;
    private final HDKeychainSigner signingWalletKeyChain;
    private final FeeCalculator feeCalculator;


    public OmniKeychainSigningService(Network network, DeterministicKeyChain deterministicKeyChain) {
        this.network = network;
        this.keyChain = deterministicKeyChain;
        this.signingWalletKeyChain = new HDKeychainSigner(deterministicKeyChain);
        feeCalculator = new HackedFeeCalculator();
    }

    /* package */ DeterministicKeyChain getKeychain() {
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

    public Network network() {
        return network;
    }
}
