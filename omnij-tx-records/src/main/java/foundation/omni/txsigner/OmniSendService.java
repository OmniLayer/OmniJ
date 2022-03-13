package foundation.omni.txsigner;

import foundation.omni.CurrencyID;
import foundation.omni.OmniValue;
import foundation.omni.netapi.omnicore.RxOmniClient;
import foundation.omni.txrecords.TransactionRecords;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.consensusj.bitcoin.json.conversion.HexUtil;
import org.consensusj.bitcoin.json.pojo.bitcore.AddressUtxoInfo;
import org.consensusj.bitcoin.signing.TransactionInputData;
import org.consensusj.bitcoin.signing.TransactionInputDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A service to sign and send Omni Transactions (similar to functionality in Omni Core)
 * TODO: To support an equivalent API to the Omni Core send functions, the ability to find
 * UTXOs for specified addresses is needed.
 * and to send transactions we need either a P2P client (e.g. PeerGroup) or server API.
 */
public class OmniSendService {
    private static final Logger log = LoggerFactory.getLogger(OmniSendService.class);

    private final RxOmniClient rxOmniClient;
    private final OmniSigningService signingService;

    public OmniSendService(RxOmniClient client, OmniSigningService signingService) {
        this.rxOmniClient = client;
        this.signingService = signingService;
    }

    public CompletableFuture<Sha256Hash> omniSend(Address fromAddress, Address toAddress, CurrencyID currency, OmniValue amount) throws IOException {
        List<AddressUtxoInfo> utxoInfos = rxOmniClient.getAddressUtxos(fromAddress);
        List<? super TransactionInputData> utxos = utxoInfos.stream().map(this::mapUtxo).toList();
        TransactionRecords.SimpleSend sendTx = new TransactionRecords.SimpleSend(toAddress, currency, amount);
        Transaction tx = signingService.omniSignTx(fromAddress, utxos, sendTx, fromAddress).join();


        tx.verify();

        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddress);
        TransactionInput input = tx.getInputs().get(0);
        input.getScriptSig()
                .correctlySpends(tx, 0, null, input.getValue(), scriptPubKey, Script.ALL_VERIFY_FLAGS);

        byte[] serializedTx = tx.bitcoinSerialize();

        Transaction deserializedTx = new Transaction(rxOmniClient.getNetParams(), serializedTx);


        CompletableFuture<Sha256Hash> sendFuture = this.sendRawTx(tx);

        return sendFuture;
        //return CompletableFuture.completedFuture(tx.getTxId());
//        return signingService
//                .omniSignTx(fromAddress, (List<TransactionInputData>) utxos, sendTx, fromAddress)
//                .thenCompose(this::sendRawTx);
    }

    private TransactionInputData mapUtxo(AddressUtxoInfo info) {
        return new TransactionInputDataImpl(rxOmniClient.getNetParams().getId(),
                info.getTxid().getBytes(),
                info.getOutputIndex(),
                info.getSatoshis(),
                info.getScript());
    }


    CompletableFuture<Sha256Hash> sendRawTx(Transaction tx) {
        return rxOmniClient.supplyAsync(() -> {
               String hexTx = HexUtil.bytesToHexString(tx.bitcoinSerialize());
               log.warn("OmniSendService: About to send tx: {}", hexTx);
               return rxOmniClient.sendRawTransaction(hexTx);
            });
    }
}
