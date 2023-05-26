package foundation.omni.txsigner;

import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.consensusj.bitcoin.json.pojo.SignedRawTransaction;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoinj.signing.FeeCalculator;
import org.consensusj.bitcoinj.signing.SigningRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
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
        return signRawTransactionWithWalletAsync(signingRequest.toUnsignedTransaction())
                .thenApply(this::asSignedTransaction);
    }

    @Override
    public FeeCalculator feeCalculator() {
        return feeCalculator;
    }

    @Override
    public Network network() {
        return client.getNetwork();
    }

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

    Transaction asSignedTransaction(SignedRawTransaction signedRawTransaction) {
        Transaction tx = new Transaction(netParams(), ByteBuffer.wrap(hexFormat.parseHex(signedRawTransaction.getHex())));
        log.info("converting to tx {}", tx);
        return tx;
    }
}
