package foundation.omni.txsigner;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.txrecords.TransactionRecords;
import foundation.omni.txrecords.UnsignedTxSimpleSend;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.consensusj.bitcoin.json.conversion.HexUtil;
import org.consensusj.bitcoin.json.pojo.UnspentOutput;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.consensusj.bitcoinj.signing.TransactionInputData;
import org.consensusj.bitcoinj.signing.TransactionInputDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A service to sign and send Omni Transactions (similar to functionality in Omni Core). Uses a JSON-RPC client to
 * connect to a ConsensusJ SPV-wallet-daemon.  (In the future we may support Bitcoin Core and Omni Core nodes as well.)
 */
public class OmniRpcClientSendingService implements OmniSendingService {
    private static final Logger log = LoggerFactory.getLogger(OmniRpcClientSendingService.class);
    private final HexFormat hexFormat = HexFormat.of();
    private final BitcoinClient client;
    private final OmniRpcClientSigningService signingService;

    public OmniRpcClientSendingService(BitcoinClient bitcoinClient) {
        client = bitcoinClient;
        this.signingService = new OmniRpcClientSigningService(bitcoinClient);
    }
    
    @Override
    public CompletableFuture<Sha256Hash> omniSend(Address fromAddress, Address toAddress, CurrencyID currency, OmniValue amount) throws IOException {
        UnsignedTxSimpleSend sendTx = assembleSimpleSend(fromAddress, toAddress, currency, amount);
        return omniSend(sendTx);
    }

    @Override
    public CompletableFuture<Sha256Hash> omniSend(UnsignedTxSimpleSend simpleSend) throws IOException {
        Transaction tx = signingService.omniSignTx(simpleSend.fromAddress(), simpleSend.inputs(), simpleSend.payload(), simpleSend.changeAddress()).join();
        // FOR DEBUGGING DON"T SEND
        //return CompletableFuture.completedFuture(Sha256Hash.ZERO_HASH);
        return sendRawTransactionAsync(tx);
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
        return new UnsignedTxSimpleSend(fromAddress, utxos, sendTx, fromAddress);
    }
    
    /**
     * Fetch inputs for an address
     * @param fromAddress Address with zero or more UTXOs
     * @return a list of all UTXOs for this address
     * @throws IOException if an error occurred fetching inputs
     */
    public List<TransactionInputData> getInputsFor(Address fromAddress) throws IOException {
        List<UnspentOutput> unspents = client.listUnspent(null, null, fromAddress);
        return unspents.stream().map(this::mapUnspent).toList();
    }

    TransactionInputData mapUnspent(UnspentOutput unspent) {
        return new TransactionInputDataImpl(
                TestNet3Params.get().getId(),
                unspent.getTxid(),
                unspent.getVout(),
                unspent.getAmount(),
                new Script(hexFormat.parseHex(unspent.getScriptPubKey())));
    }

    public CompletableFuture<Sha256Hash> sendRawTransactionAsync(Transaction tx) {
        log.info("Preparing to send: {}", tx);
        return client.supplyAsync(() -> {
            String hexTx = HexUtil.bytesToHexString(tx.bitcoinSerialize());
            log.warn("OmniSendingService: About to send tx: {}", hexTx);
            return client.sendRawTransaction(tx);
        });
    }

}
