package foundation.omni.txsigner

import foundation.omni.CurrencyID
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniValue
import foundation.omni.txrecords.TransactionRecords
import foundation.omni.txrecords.UnsignedTxSimpleSend
import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Network
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
 * Tests an SPV signing server backed by a Bitcoin/Omni Core node (both on RegTest)
 */
@Ignore("This is actually a RegTest integration test")
class OmniRpcClientSigningSpvRegTest extends Specification {
    static final CurrencyID currencyID = CurrencyID.OMNI
    static final OmniValue amount =  OmniDivisibleValue.ofWilletts(1)
    static final URI serverUri = RpcURI.DEFAULT_REGTEST_URI

    def "basic test"() {
        given: "Setup similar to that in SendTool"
        Network network = BitcoinNetwork.REGTEST

        RpcConfig config = new RpcConfig(network, serverUri, "bitcoinrpc", "pass");
        var rpcClient = new BitcoinExtendedClient(config)
                .withWallet(BitcoinExtendedClient.REGTEST_WALLET_NAME,config.username, config.password)
        rpcClient.initRegTestWallet()
        var fundingSource = new RegTestFundingSource(rpcClient)
        var spvClient = new BitcoinExtendedClient(network, URI.create("http://localhost:8080"), "", "")
        Address fromAddress = spvClient.getNewAddress()
        var fundingTxId = fundingSource.requestBitcoin(fromAddress, 1.btc)
        Address toAddress = rpcClient.getNewAddress()
        var blockHash = rpcClient.generateBlocks(1).get(0)
        while (spvClient.getBlockChainInfo().bestBlockHash != blockHash) {
            sleep(100)
        }

        OmniRpcClientSendingService sendService = new OmniRpcClientSendingService(spvClient)
        OmniRpcClientSigningService signService = new OmniRpcClientSigningService(spvClient)

        when: "We assemble the unsigned transaction"
        TransactionRecords.SimpleSend simpleSend = new TransactionRecords.SimpleSend(toAddress, currencyID, amount )
        List<TransactionInputData> inputs = sendService.getInputsFor(fromAddress)
        UnsignedTxSimpleSend unsigned = new UnsignedTxSimpleSend(fromAddress, inputs, simpleSend, fromAddress)

        and: "We sign the transaction"
        Transaction signedTx = signService.omniSignTx(unsigned).join()

        and: "We do some verification"
        Transaction.verify(network, signedTx)
        Script scriptPubKey = ScriptBuilder.createOutputScript(fromAddress);
        TransactionInput input = signedTx.getInputs().get(0)
        input.getScriptSig()
                .correctlySpends(signedTx, 0, null, input.getValue(), scriptPubKey, Script.ALL_VERIFY_FLAGS);

        var serializedTx = ByteBuffer.wrap(signedTx.serialize())

        Transaction deserializedTx = Transaction.read(serializedTx)

        then: "No exception was thrown and deserializedTx is not null"
        deserializedTx != null
    }
}
