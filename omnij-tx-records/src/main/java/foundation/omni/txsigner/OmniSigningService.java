package foundation.omni.txsigner;

import foundation.omni.tx.ClassCEncoder;
import foundation.omni.tx.Transactions;
import foundation.omni.txrecords.UnsignedTxSimpleSend;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.TestNet3Params;
import org.consensusj.bitcoinj.signing.DefaultSigningRequest;
import org.consensusj.bitcoinj.signing.FeeCalculator;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.consensusj.bitcoinj.signing.SigningUtils;
import org.consensusj.bitcoinj.signing.TransactionInputData;
import org.consensusj.bitcoinj.signing.TransactionOutputData;
import org.consensusj.bitcoinj.signing.TransactionOutputOpReturn;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public interface OmniSigningService {

    default CompletableFuture<Transaction> omniSignTx(Address fromAddress, List<TransactionInputData> inputUtxos, Transactions.OmniTx omniTx, Address changeAddress, Address redeemAddress, Coin referenceAmount) {
        throw new UnsupportedOperationException("no redeemaddress support yet");
    }

    default CompletableFuture<Transaction> omniSignTx(Address fromAddress, List<? super TransactionInputData> inputUtxos, Transactions.OmniTx omniTx, Address changeAddress) {
        SigningRequest signingRequest = createOmniClassCSigningRequest(fromAddress, inputUtxos, omniTx, changeAddress);
        return signTx(signingRequest);
    }

    default CompletableFuture<Transaction> omniSignTx(UnsignedTxSimpleSend unsignedTx) {
        return omniSignTx(unsignedTx.fromAddress(), unsignedTx.inputs(), unsignedTx.payload(), unsignedTx.changeAddress());
    }

    /**
     * Sign a Bitcoin transaction (possibly with an embedded Omni Class C or Class B transaction)
     *
     * @param signingRequest a ConsensusJ signing request
     * @return a signed bitcoinj transaction
     */
    CompletableFuture<Transaction> signTx(SigningRequest signingRequest);

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
    default SigningRequest createOmniClassCSigningRequest(Address fromAddress, List<? super TransactionInputData> inputUtxos, Transactions.OmniTx omniTx, Address changeAddress) {
        // Create a signing request with just the OP_RETURN output
        SigningRequest request = new DefaultSigningRequest(netParams(), (List<TransactionInputData>) inputUtxos, List.of(createOpReturn(omniTx)));

        // Add a reference address output if necessary
        Address refAddress = (omniTx instanceof Transactions.OmniRefTx refTx) ? refTx.referenceAddress() : null;
        if (refAddress != null) {
            //request = request.addDustOutput(refAddress);
            request = request.addOutput(refAddress, Coin.MICROCOIN);
        }
        try {
            return SigningUtils.addChange(request, changeAddress, feeCalculator());
        } catch (InsufficientMoneyException ime) {
            throw new RuntimeException(ime);
        }
    }

    default SigningRequest createOmniClassCSigningRequest(UnsignedTxSimpleSend unsignedTxSimpleSend) {
        return createOmniClassCSigningRequest(unsignedTxSimpleSend.fromAddress(),
                unsignedTxSimpleSend.inputs(),
                unsignedTxSimpleSend.payload(),
                unsignedTxSimpleSend.changeAddress());
    }

    FeeCalculator feeCalculator();

    @Deprecated
    default NetworkParameters netParams() {
        return TestNet3Params.get();
    }

    default TransactionOutputData createOpReturn(Transactions.OmniTx omniTx) {
        byte[] unprefixedPayload = omniTx.payload();
        byte[] opReturnData = ClassCEncoder.addOmniPrefix(unprefixedPayload);
        return new TransactionOutputOpReturn(TestNet3Params.get().getId(), opReturnData);
    }

    class HackedFeeCalculator implements FeeCalculator {

        @Override
        public Coin calculateFee(SigningRequest signingRequest) {
            return Coin.ofSat(2570);
        }
    }
}
