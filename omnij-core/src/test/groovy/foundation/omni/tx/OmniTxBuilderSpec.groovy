package foundation.omni.tx

import foundation.omni.OmniDivisibleValue
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.InsufficientMoneyException
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.params.MainNetParams
import org.spongycastle.util.encoders.Hex
import spock.lang.Specification

import static foundation.omni.CurrencyID.MSC


/**
 *
 */
class OmniTxBuilderSpec extends BaseTxSpec {
    static final OmniTxBuilder omniTxBuilder = new OmniTxBuilder(netParams)

    def "Build a transaction using OmniTxBuilder with insufficient funds throws InsufficientMoneyException" () {
        when:
        def toAddress = senderAddr
        // TODO: Are there mock UTXOs in bitcoinj?
//        TransactionOutput utxo = new TransactionOutput(netParams, null, Coin.COIN, senderKey)
//        List<TransactionOutput> utxos = [utxo]
        List<TransactionOutput> utxos = []
        def tx = omniTxBuilder.createSignedSimpleSend(senderKey, utxos, toAddress, MSC, OmniDivisibleValue.ofWillets(10000000))
        byte[] rawTx = tx.bitcoinSerialize()

        then:
        InsufficientMoneyException e = thrown()
        e.message == "Insufficient Bitcoin to build Omni Transaction"

    }

}