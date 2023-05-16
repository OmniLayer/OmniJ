package foundation.omni.sendtool;

import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.txsigner.OmniRpcClientSendingService;
import foundation.omni.txsigner.OmniSendingService;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.TestNet3Params;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * Create, sign, and send an Omni Simple-Send transaction using a local Bitcoin node. The local
 * Bitcoin node can be Bitcoin Core or the ConsensusJ SPV Daemon. This tool does not verify Omni
 * balances before signing or sending, so a transaction may be built for which there are insufficient funds.
 */
public class OmniLiteSend {
    private final OmniRpcClientSendingService omniSendingService;

    public static void main(String[] args) throws IOException {
        String arg0, arg1, arg2, arg3;
        if (args.length < 4) {
            arg0 = "mq9GZtX1fq2DnerX2Cd8HSAQAVVMmPCVu1";    // Source Address  muuZ2RXkePUsx9Y6cWt3TCSbQyetD6nKak
            arg1 = "muuZ2RXkePUsx9Y6cWt3TCSbQyetD6nKak";    // Dest Address (was 'mzFyqtcLU6Gkp9e4qqsGK7buiuH4HEcW1q')
            arg2 = "1";                                     // Currency ID
            arg3 = "80112";                                // Amount in willetts
        } else {
            arg0 = args[0];
            arg1 = args[1];
            arg2 = args[2];
            arg3 = args[3];
        }

        // Parse Parameters
        Address fromAddress = Address.fromString(null, arg0);
        Address toAddress = Address.fromString(null, arg1);
        CurrencyID id = CurrencyID.of(Long.parseLong(arg2));
        OmniDivisibleValue amount = OmniDivisibleValue.ofWilletts(Long.parseLong(arg3));

        OmniLiteSend sendTool = new OmniLiteSend();
        var futureHash = sendTool.send(fromAddress, toAddress, id, amount);
        var txid = futureHash.join();
        System.out.println("Sent TXID: " + txid);
    }

    public OmniLiteSend() {
        BitcoinClient rpcClient = new BitcoinClient(TestNet3Params.get(), URI.create("http://localhost:8080"), "", "");
        omniSendingService = new OmniRpcClientSendingService(rpcClient);
    }

    public CompletableFuture<Sha256Hash> send(Address fromAddress, Address toAddress, CurrencyID currencyId, OmniDivisibleValue amount) throws IOException {
        return omniSendingService.omniSend(fromAddress, toAddress, currencyId, amount);
    }
}
