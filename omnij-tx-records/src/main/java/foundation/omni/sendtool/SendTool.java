package foundation.omni.sendtool;

import foundation.omni.CurrencyID;
import foundation.omni.OmniDivisibleValue;
import foundation.omni.rpc.OmniClient;
import foundation.omni.txsigner.OmniKeychainSendingService;
import foundation.omni.txsigner.OmniKeychainSigningService;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.AddressParser;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.consensusj.bitcoin.jsonrpc.RpcConfig;
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

/**
 *  Omni transaction sending tool that uses a local keychain and a remote, address-indexed Omni Core server (or OmniProxy)
 */
public class SendTool {
    private static final AddressParser addressParser = AddressParser.getDefault();
    public static final String mnemonicString = "panda diary marriage suffer basic glare surge auto scissors describe sell unique";
    public static final Instant creationInstant = LocalDate.of(2019, 4, 10).atStartOfDay().toInstant(ZoneOffset.UTC);
    private final OmniKeychainSigningService signingService;
    private final OmniKeychainSendingService sendService;

    public static void main(String[] args) throws IOException {
        SendTool sendTool = new SendTool();
        String arg0, arg1, arg2, arg3;
        if (args.length < 4) {
            arg0 = "mq9GZtX1fq2DnerX2Cd8HSAQAVVMmPCVu1";    // Source Address
            arg1 = "mzFyqtcLU6Gkp9e4qqsGK7buiuH4HEcW1q";    // Dest Address
            arg2 = "1";                                     // Currency ID
            arg3 = "1";                                     // Amount in willetts
        } else {
            arg0 = args[0];
            arg1 = args[1];
            arg2 = args[2];
            arg3 = args[3];
        }


        Address fromAddress = addressParser.parseAddress(arg0);
        Address toAddress = addressParser.parseAddress(arg1);
        CurrencyID id = CurrencyID.of(Long.parseLong(arg2));
        OmniDivisibleValue amount = OmniDivisibleValue.ofWilletts(Long.parseLong(arg3));
        var futureHash = sendTool.send(fromAddress, toAddress, id, amount);
        var txid = futureHash.join();
        System.out.println("Sent TXID: " + txid);
    }

    public SendTool() {
        Network network = BitcoinNetwork.TESTNET;
        int signingAccountIndex = 0;
        ScriptType outputScriptType = ScriptType.P2PKH;
        DeterministicSeed seed = setupTestSeed();

        BipStandardDeterministicKeyChain signingKeychain = new BipStandardDeterministicKeyChain(seed, outputScriptType, network);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        signingKeychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 20);  // Generate first 2 receiving address
        signingKeychain.getKeys(KeyChain.KeyPurpose.CHANGE, 20);         // Generate first 2 change address

        signingService = new OmniKeychainSigningService(network, signingKeychain);


        URI omniProxyTestNetURI = URI.create("http://192.168.8.177:18332");
        RpcConfig config = new RpcConfig(network, omniProxyTestNetURI, "bitcoinrpc", "pass");
        var omniProxyClient = new OmniClient(config.network(),
                config.getURI(),
                config.getUsername(),
                config.getPassword(),
                false,
                false);

        sendService = new OmniKeychainSendingService(omniProxyClient, signingService);
    }

    public CompletableFuture<Sha256Hash> send(Address fromAddress, Address toAddress, CurrencyID currencyId, OmniDivisibleValue amount) throws IOException {
        return sendService.omniSend(fromAddress, toAddress, currencyId, amount);
    }

    static DeterministicSeed setupTestSeed() {
        return DeterministicSeed.ofMnemonic(mnemonicString, "", creationInstant);
    }
}
