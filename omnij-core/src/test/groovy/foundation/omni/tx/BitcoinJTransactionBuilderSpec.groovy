package foundation.omni.tx

import foundation.omni.net.OmniNetworkParameters
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.spongycastle.util.encoders.Hex
import spock.lang.Specification

import static foundation.omni.CurrencyID.MSC


/**
 * WIP: Unit tests of building transactions
 * This code is currently really poor - not to be used as an example
 *
 * TODO: Make this test into a real test
 *
 */
class BitcoinJTransactionBuilderSpec extends Specification {
    static final netParams = MainNetParams.get()
    static final omniParams = OmniNetworkParameters.fromBitcoinParms(netParams)
    static final RawTxBuilder builder = new RawTxBuilder()
    static final OmniTxBuilder omniTxBuilder = new OmniTxBuilder(netParams)

    final static BigInteger NotSoPrivatePrivateKey = new BigInteger(1, Hex.decode("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));
    static final senderKey = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)
    static final senderAddr = senderKey.toAddress(netParams)

    def "Build a transaction using EncodeMultisig.encodeObfuscated and BitcoinJ"() {
        given:
        EncodeMultisig encoder = new EncodeMultisig(RegTestParams.get())

        when:
        def toAddress = senderAddr
        def txHex = builder.createSimpleSendHex(MSC, Coin.COIN.longValue())
        def payload = hex(txHex)
        Transaction tx = encoder.encodeObfuscated(senderKey, payload, senderAddr.toString())
        tx.addOutput(Coin.MILLICOIN, omniParams.exodusAddress)
        tx.addOutput(Coin.CENT, toAddress)
        Script script = ScriptBuilder.createInputScript(null)
        def outPoint = new TransactionOutPoint(netParams, 1, Sha256Hash.create("boppitybop".getBytes()))

        TransactionInput input = new TransactionInput(netParams, null, script.program, outPoint, null)
        tx.addInput(input)
        byte[] rawTx = tx.bitcoinSerialize()

        then:
        rawTx != null
        rawTx.length > 20
        rawTx.encodeHex().toString() == "01000000014fc80ee90baa51dcfa12d1a77dbd0e3820694c52327a056741c379eceb71df2e010000000100ffffffff03a0860100000000006751410401de173aa944eacf7e44e5073baca93fb34fe4b7897a1c82c92dfdc8a1f75ef58cd1b06e8052096980cb6e1ad6d3df143c34b3d7394bae2782a4df570554c2fb2103667d08778ca7e25f4044f96377023127ce00000000000000000000000000000052aea0860100000000001976a914946cb2e08075bcbaf157e47bcb67eb2b2339d24288ac40420f00000000001976a914ae38fb8f96d6a430feda6ccc5bbdc944c80832cc88ac00000000"
    }

    def "Build a transaction using OmniTxBuilder" () {
        when:
        def toAddress = senderAddr
        // TODO: Are there mock UTXOs in bitcoinj?
//        TransactionOutput utxo = new TransactionOutput(netParams, null, Coin.COIN, senderKey)
//        List<TransactionOutput> utxos = [utxo]
        List<TransactionOutput> utxos = []
        def tx = omniTxBuilder.createSignedSimpleSend(senderKey, utxos, toAddress, MSC, 10000000)
        byte[] rawTx = tx.bitcoinSerialize()

        then:
        rawTx != null
        rawTx.length > 20

        // TODO: Emtpy utxo list should throw an exception

    }

    private byte[] hex(String string) {
        return string.decodeHex()
    }

}