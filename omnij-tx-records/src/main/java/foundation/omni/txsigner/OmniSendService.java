package foundation.omni.txsigner;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.netapi.omnicore.RxOmniClient;
import foundation.omni.txrecords.TransactionRecords;
import foundation.omni.txrecords.UnsignedTxSimpleSend;
import org.bitcoinj.core.*;
import org.consensusj.bitcoin.json.conversion.HexUtil;
import org.consensusj.bitcoin.json.pojo.bitcore.AddressUtxoInfo;
import org.consensusj.bitcoinj.signing.DefaultSigningRequest;
import org.consensusj.bitcoinj.signing.FeeCalculator;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.consensusj.bitcoinj.signing.SigningUtils;
import org.consensusj.bitcoinj.signing.TransactionInputData;
import org.consensusj.bitcoinj.signing.TransactionInputDataImpl;
import org.consensusj.bitcoinj.signing.TransactionOutputAddress;
import org.consensusj.bitcoinj.signing.TransactionOutputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A service to sign and send Omni Transactions (similar to functionality in Omni Core)
 */
public class OmniSendService {
    private static final Logger log = LoggerFactory.getLogger(OmniSendService.class);

    private final RxOmniClient rxOmniClient;
    private final OmniSigningService signingService;
    private final FeeCalculator feeCalculator;

    public OmniSendService(RxOmniClient client, OmniSigningService signingService) {
        this.rxOmniClient = client;
        this.signingService = signingService;
        feeCalculator = new HackedFeeCalculator();
    }

    // Simple Bitcoin send, from receive address 0 with change to sending address
    // This is for testing (e.g. for sending TBTC to the moneyman address)
    public CompletableFuture<Sha256Hash> bitcoinSend(Address toAddress, Coin amount) throws IOException {
        Address fromAddress = signingService.getKeychain().receivingAddr(0);    // Hardcode to receive address 0
        List<TransactionInputData> utxos = getInputsFor(fromAddress);
        TransactionOutputData outputData = new TransactionOutputAddress(amount, toAddress);
        SigningRequest bitcoinSendReq = createBitcoinSigningRequest(fromAddress, utxos, List.of(outputData), fromAddress);
        CompletableFuture<Transaction> futureTransaction = signingService.signTx(bitcoinSendReq);
        return futureTransaction.thenApply(Transaction::getTxId);
    }

    public SigningRequest createBitcoinSigningRequest(Address fromAddress, List<? super TransactionInputData> inputUtxos, List<TransactionOutputData> outputs, Address changeAddress) {
        // Create a signing request with just the OP_RETURN output
        SigningRequest request = new DefaultSigningRequest(fromAddress.getParameters(), (List<TransactionInputData>) inputUtxos, outputs);
        try {
            return SigningUtils.addChange(request, changeAddress, feeCalculator);
        } catch (InsufficientMoneyException ime) {
            throw new RuntimeException(ime);
        }
    }

    static class HackedFeeCalculator implements FeeCalculator {
        @Override
        public Coin calculateFee(SigningRequest signingRequest) {
            return Coin.ofSat(257);
        }
    }

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
    public CompletableFuture<Sha256Hash> omniSend(Address fromAddress, Address toAddress, CurrencyID currency, OmniValue amount) throws IOException {
        UnsignedTxSimpleSend sendTx = assembleSimpleSend(fromAddress, toAddress, currency, amount);
        return omniSend(sendTx);
    }

    /**
     * Sign and Send an Omni Simple Send transaction
     *
     * @param simpleSend An object holding all the parameters (including UTXO inputs) for a simple send transaction
     * @return A future for the transaction hash
     * @throws IOException A failure occurred fetching broadcasting the transaction
     */
    public CompletableFuture<Sha256Hash> omniSend(UnsignedTxSimpleSend simpleSend) throws IOException {
        Transaction tx = signingService.omniSignTx(simpleSend.fromAddress(), simpleSend.inputs(), simpleSend.payload(), simpleSend.changeAddress()).join();
        CompletableFuture<Sha256Hash> sendFuture = this.sendRawTx(tx);
        return sendFuture;
//        return signingService
//                .omniSignTx(fromAddress, (List<TransactionInputData>) utxos, sendTx, fromAddress)
//                .thenCompose(this::sendRawTx);
    }

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
    public UnsignedTxSimpleSend assembleSimpleSend(Address fromAddress, Address toAddress, CurrencyID currency, OmniValue amount) throws IOException {
        List<TransactionInputData> utxos = getInputsFor(fromAddress);
        TransactionRecords.SimpleSend sendTx = new TransactionRecords.SimpleSend(toAddress, currency, amount);
        return new UnsignedTxSimpleSend(fromAddress, utxos, sendTx, toAddress);
    }

    /**
     * Fetch inputs for an address
     * @param fromAddress Address with zero or more UTXOs
     * @return a list of all UTXOs for this address
     * @throws IOException if an error occurred fetching inputs
     */
    public List<TransactionInputData> getInputsFor(Address fromAddress) throws IOException {
        List<AddressUtxoInfo> utxoInfos = rxOmniClient.getAddressUtxos(fromAddress);
        return utxoInfos.stream().map(this::mapUtxo).toList();
    }

    private TransactionInputData mapUtxo(AddressUtxoInfo info) {
        return new TransactionInputDataImpl(rxOmniClient.getNetParams().getId(),
                info.getTxid().getBytes(),
                info.getOutputIndex(),
                info.getSatoshis(),
                info.getScript());
    }


    CompletableFuture<Sha256Hash> sendRawTx(Transaction tx) {
        log.info("OmniSendService: preparing to send: {}", tx);
        return rxOmniClient.supplyAsync(() -> {
               String hexTx = HexUtil.bytesToHexString(tx.bitcoinSerialize());
               log.warn("OmniSendService: About to send tx: {}", hexTx);
               return rxOmniClient.sendRawTransaction(hexTx);
            });
    }
}
