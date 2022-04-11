package foundation.omni.txsigner;

import foundation.omni.txrecords.UnsignedTxSimpleSend;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.signing.DefaultSigningRequest;
import org.consensusj.bitcoin.signing.FeeCalculator;
import org.consensusj.bitcoin.signing.SigningRequest;
import org.consensusj.bitcoin.signing.SigningUtils;
import org.consensusj.bitcoin.signing.HDKeychainSigner;
import org.consensusj.bitcoin.signing.TestnetFeeCalculator;
import org.consensusj.bitcoin.signing.TransactionInputData;
import org.consensusj.bitcoin.signing.TransactionOutputData;
import org.consensusj.bitcoin.signing.TransactionOutputOpReturn;
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain;
import foundation.omni.tx.ClassCEncoder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static foundation.omni.tx.Transactions.OmniTx;
import static foundation.omni.tx.Transactions.OmniRefTx;

/**
 * A service to sign Omni transactions
 */
public class OmniSigningService {
    private final NetworkParameters netParams;
    private final HDKeychainSigner signingWalletKeyChain;
    private final FeeCalculator feeCalculator;

    static class HackedFeeCalculator implements FeeCalculator {

        @Override
        public Coin calculateFee(SigningRequest signingRequest) {
            return Coin.ofSat(257);
        }
    }

    public OmniSigningService(NetworkParameters netParams, BipStandardDeterministicKeyChain deterministicKeyChain) {
        this.netParams = netParams;
        this.signingWalletKeyChain = new HDKeychainSigner(deterministicKeyChain);
        feeCalculator = new HackedFeeCalculator();
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


    public CompletableFuture<Transaction> omniSignTx(Address fromAddress, List<TransactionInputData> inputUtxos, OmniTx omniTx, Address changeAddress, Address redeemAddress, Coin referenceAmount) {
        throw new UnsupportedOperationException("no redeemaddress support yet");
    }

    public CompletableFuture<Transaction> omniSignTx(Address fromAddress, List<? super TransactionInputData> inputUtxos,  OmniTx omniTx, Address changeAddress) {
        SigningRequest signingRequest = createOmniClassCSigningRequest(fromAddress, inputUtxos, omniTx, changeAddress);
        return signTx(signingRequest);
    }

    public CompletableFuture<Transaction> omniSignTx(UnsignedTxSimpleSend unsignedTx) {
        return omniSignTx(unsignedTx.fromAddress(), unsignedTx.inputs(), unsignedTx.payload(), unsignedTx.changeAddress());
    }

    /**
     * Sign a Bitcoin transaction (possibly with an embedded Omni Class C or Class B transaction)
     *
     * @param signingRequest a ConsensusJ signing request
     * @return a signed bitcoinj transaction
     */
    public CompletableFuture<Transaction> signTx(SigningRequest signingRequest) {
        return signingWalletKeyChain.signTransaction(signingRequest);
    }

    /**
     * Build a {@link SigningRequest} for an Omni transaction. Performs the following:
     * <ol>
     *     <li>Includes all inputs in the transaction (TBD: choose minimal/optional subset)</li>
     *     <li>Creates an OP_RETURN output with the payload</li>
     *     <li>Creates a reference address output if necessary</li>
     *     <li>Adds a change address (if there's change)</li>
     * </ol>
     * @param fromAddress The sending/signing address
     * @param inputUtxos List of UTXOs for funding the transaction
     * @param omniTx The Omni transaction payload to send
     * @param changeAddress Address to return bitcoin change to
     * @return A ConsensusJ SigningRequest for the transaction
     */
    public SigningRequest createOmniClassCSigningRequest(Address fromAddress, List<? super TransactionInputData> inputUtxos, OmniTx omniTx, Address changeAddress) {
        // Create a signing request with just the OP_RETURN output
        SigningRequest request = new DefaultSigningRequest(netParams, (List<TransactionInputData>) inputUtxos, List.of(createOpReturn(omniTx)));

        // Add a reference address output if necessary
        Address refAddress = (omniTx instanceof OmniRefTx refTx) ? refTx.referenceAddress() : null;
        if (refAddress != null) {
            request = request.addDustOutput(refAddress);
        }
        return SigningUtils.addChange(request, changeAddress, feeCalculator);
    }

    private TransactionOutputData createOpReturn(OmniTx omniTx) {
        byte[] unprefixedPayload = omniTx.payload();
        byte[] opReturnData = ClassCEncoder.addOmniPrefix(unprefixedPayload);
        return new TransactionOutputOpReturn(netParams.getId(), opReturnData);
    }
}
