package foundation.omni.txsigner

import foundation.omni.CurrencyID
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniValue
import foundation.omni.txrecords.TransactionRecords
import foundation.omni.txrecords.UnsignedTxSimpleSend
import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Network
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient
import org.consensusj.bitcoin.jsonrpc.RpcConfig
import org.consensusj.bitcoin.jsonrpc.RpcURI
import org.consensusj.bitcoin.jsonrpc.test.RegTestFundingSource
import org.consensusj.bitcoinj.signing.TransactionInputData
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.ByteBuffer

/**
 * RegTest Functional test to test RPC Client signing
 */
@Ignore("This is actually a RegTest integration test")
class OmniRpcClientSigningRegTest extends  Specification {
    static final CurrencyID currencyID = CurrencyID.OMNI
    static final OmniValue amount =  OmniDivisibleValue.ofWilletts(1)
    static final URI serverUri = RpcURI.DEFAULT_REGTEST_URI

    def "basic test"() {
        given: "Setup similar to that in SendTool"
        Network network = BitcoinNetwork.REGTEST
        NetworkParameters netParams = NetworkParameters.of(network)

        RpcConfig config = new RpcConfig(network, serverUri, "bitcoinrpc", "pass");
        var rpcClient = new BitcoinExtendedClient(config)
                .withWallet(BitcoinExtendedClient.REGTEST_WALLET_NAME,config.username, config.password)
        rpcClient.initRegTestWallet()
        var fundingSource = new RegTestFundingSource(rpcClient)
        Address fromAddress = fundingSource.createFundedAddress(1.btc)
        Address toAddress = rpcClient.getNewAddress()
        rpcClient.generateBlocks(1)

        OmniRpcClientSendingService sendService = new OmniRpcClientSendingService(rpcClient)
        OmniRpcClientSigningService signService = new OmniRpcClientSigningService(rpcClient)

        when: "We assemble the unsigned transaction"
        TransactionRecords.SimpleSend simpleSend = new TransactionRecords.SimpleSend(toAddress, currencyID, amount )
        List<TransactionInputData> inputs = sendService.getInputsFor(fromAddress)
        UnsignedTxSimpleSend unsigned = new UnsignedTxSimpleSend(fromAddress, inputs, simpleSend, fromAddress)

        and: "We sign the transaction"
        Transaction signedTx = signService.omniSignTx(unsigned).join()

        and: "We do some verification"
        signedTx.verify()
        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddress);
        TransactionInput input = signedTx.getInputs().get(0)
        input.getScriptSig()
                .correctlySpends(signedTx, 0, null, input.getValue(), scriptPubKey, Script.ALL_VERIFY_FLAGS);

        var serializedTx = ByteBuffer.wrap(signedTx.bitcoinSerialize())

        Transaction deserializedTx = new Transaction(netParams, serializedTx)

        then: "No exception was thrown and deserializedTx is not null"
        deserializedTx != null
    }
}
