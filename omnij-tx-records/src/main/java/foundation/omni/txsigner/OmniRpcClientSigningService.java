package foundation.omni.txsigner;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;
import org.consensusj.bitcoin.json.conversion.HexUtil;
import org.consensusj.bitcoin.json.pojo.SignedRawTransaction;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoinj.signing.FeeCalculator;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class OmniRpcClientSigningService implements OmniSigningService {
    private static final Logger log = LoggerFactory.getLogger(OmniRpcClientSigningService.class);
    private final HexFormat hexFormat = HexFormat.of();
    private final BitcoinClient client;
    private final FeeCalculator feeCalculator;

    public OmniRpcClientSigningService(BitcoinClient bitcoinClient) {
        client = bitcoinClient;
        feeCalculator = new HackedFeeCalculator();
    }

    @Override
    public CompletableFuture<Transaction> signTx(SigningRequest signingRequest) {
        return signRawTransactionWithWalletAsync(asUnsignedTransaction(signingRequest))
                .thenApply(this::asSignedTransaction);
    }

    @Override
    public FeeCalculator feeCalculator() {
        return feeCalculator;
    }

    @Override
    public NetworkParameters netParams() {
        return client.getNetParams();
    }

    public CompletableFuture<SignedRawTransaction> signRawTransactionWithWalletAsync(Transaction tx) {
        log.info("Preparing to sign: {}", tx);
        return client.supplyAsync(() -> {
            String hexTx = hexFormat.formatHex(tx.bitcoinSerialize());
            log.warn("OmniSendingService: About to send tx: {}", hexTx);
            return client.signRawTransactionWithWallet(hexTx);
        });
    }

    Transaction asUnsignedTransaction(SigningRequest signingRequest) {
        NetworkParameters params = TestNet3Params.get();
        Transaction unsignedTx = new Transaction(TestNet3Params.get());
        signingRequest.inputs().forEach(in ->
                unsignedTx.addInput(new TransactionInput(params,
                        unsignedTx,
                        new byte[0],
                        in.toOutPoint(),
                        in.amount())));
        signingRequest.outputs().forEach(out ->
                unsignedTx.addOutput(new TransactionOutput(params,
                        unsignedTx,
                        out.amount(),
                        out.script().getProgram())));
        return unsignedTx;
    }

    Transaction asSignedTransaction(SignedRawTransaction signedRawTransaction) {
        Transaction tx = new Transaction(netParams(), hexFormat.parseHex(signedRawTransaction.getHex()));
        log.info("converting to tx {}", tx);
        return tx;
    }
}
