package foundation.omni.txsigner

import foundation.omni.CurrencyID
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniValue
import foundation.omni.netapi.omnicore.RxOmniClient
import foundation.omni.txrecords.UnsignedTxSimpleSend
import org.bitcoinj.core.Address
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.KeyChain
import org.bitcoinj.wallet.UnreadableWalletException
import org.consensusj.bitcoin.rpc.RpcConfig
import org.consensusj.bitcoinj.wallet.BipStandardDeterministicKeyChain
import spock.lang.Ignore
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Functional test of Signing Service that uses SendService to fetch inputs
 */
@Ignore("Functional test requires a server")
class OmniSigningServiceSpec extends Specification {
    static final String mnemonicString = "panda diary marriage suffer basic glare surge auto scissors describe sell unique";
    static final Instant creationInstant = LocalDate.of(2019, 4, 10).atStartOfDay().toInstant(ZoneOffset.UTC)
    static final Address fromAddress = Address.fromString(null, "mq9GZtX1fq2DnerX2Cd8HSAQAVVMmPCVu1")
    static final Address toAddress = Address.fromString(null, "mzFyqtcLU6Gkp9e4qqsGK7buiuH4HEcW1q")
    static final CurrencyID currencyID = CurrencyID.OMNI
    static final OmniValue amount =  OmniDivisibleValue.ofWilletts(1)
    static final URI omniProxyUri = URI.create("http://192.168.8.177:18332")

    def "basic test"() {
        given: "Setup similar to that in SendTool"
        NetworkParameters netParams = TestNet3Params.get()
        int signingAccountIndex = 0
        Script.ScriptType outputScriptType = Script.ScriptType.P2PKH
        DeterministicSeed seed = setupTestSeed()

        BipStandardDeterministicKeyChain signingKeychain = new BipStandardDeterministicKeyChain(seed, outputScriptType, netParams, signingAccountIndex);
        // We need to create some leaf keys in the HD keychain so that they can be found for verifying transactions
        signingKeychain.getKeys(KeyChain.KeyPurpose.RECEIVE_FUNDS, 20)  // Generate first 2 receiving address
        signingKeychain.getKeys(KeyChain.KeyPurpose.CHANGE, 20)         // Generate first 2 change address

        URI omniProxyTestNetURI = omniProxyUri
        RpcConfig config = new RpcConfig(netParams, omniProxyTestNetURI, "bitcoinrpc", "pass");
        var omniProxyClient = new RxOmniClient(config.getNetParams(),
                config.getURI(),
                config.getUsername(),
                config.getPassword(),
                false,
                false);


        OmniSigningService signService = new OmniSigningService(netParams, signingKeychain)
        OmniSendService sendService = new OmniSendService(omniProxyClient, signService)

        when: "We assemble the unsigned transaction"
        UnsignedTxSimpleSend sendTx = sendService.assembleSimpleSend(fromAddress, toAddress, currencyID, amount)

        and: "We sign the transaction"
        Transaction tx = signService.omniSignTx(sendTx).join()

        and: "We do some verification"
        tx.verify()
        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddress);
        TransactionInput input = tx.getInputs().get(0)
        input.getScriptSig()
                .correctlySpends(tx, 0, null, input.getValue(), scriptPubKey, Script.ALL_VERIFY_FLAGS);

        byte[] serializedTx = tx.bitcoinSerialize()

        Transaction deserializedTx = new Transaction(rxOmniClient.getNetParams(), serializedTx)

        then: "No exception was thrown and deserializedTx is not null"
        deserializedTx != null
    }

    DeterministicSeed setupTestSeed() {
        try {
            return new DeterministicSeed(mnemonicString, null, "", creationInstant.getEpochSecond())
        } catch (UnreadableWalletException e) {
            throw new RuntimeException(e)
        }
    }
}
