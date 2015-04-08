package foundation.omni.test.tx

import com.msgilligan.bitcoin.BTC
import foundation.omni.BaseRegTestSpec
import foundation.omni.tx.OmniTxBuilder
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.params.RegTestParams

import static foundation.omni.CurrencyID.MSC

/**
 * Test simple sends using send_MP and OmniTXBuilder and sendRawTransaction
 */
class ClientSideRawTxSpec extends BaseRegTestSpec {
    static final netParams = RegTestParams.get()

    static final BigDecimal startBTC = 10.0
    static final BigDecimal startMSC = 1000.0

    def "Can simple send amount MSC from one address to another using send_MP"() {
        given:
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        BigDecimal amount = 0.1

        when: "we send MSC"
        def startBalance = getbalance_MP(fundedAddress, MSC).balance
        def toAddress = getNewAddress()
        def txid = send_MP(fundedAddress, toAddress, MSC, amount)

        and: "a block is generated"
        generateBlock()
        def endBalance = getbalance_MP(fundedAddress, MSC).balance

        then: "the toAddress has the correct MSC balance and source address is reduced by correct amount"
        amount == getbalance_MP(toAddress, MSC).balance
        endBalance == startBalance - amount
    }

    def "Can simple send amount MSC from one address to another using OmniTxBuilder and sendraw RPC"() {
        given:
        def omniTxBuilder = new OmniTxBuilder()
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def fundedKey = dumpPrivKey(fundedAddress)
        BigDecimal amount = 0.1

        when: "we build a signed Omni Simple Send Transaction in a bitcoinj Transaction object"
        def startBalance = getbalance_MP(fundedAddress, MSC).balance
        def toAddress = getNewAddress()

        List<TransactionOutput> utxos = listUnspentJ(fundedAddress)
        def tx = omniTxBuilder.createSignedSimpleSend(fundedKey, utxos, toAddress, MSC, BTC.btcToCoin(amount).longValue())

        and: "we send it"
        def txid = sendRawTransaction(tx)

        and: "a block is generated"
        generateBlock()
        def endBalance = getbalance_MP(fundedAddress, MSC).balance

        then: "the toAddress has the correct MSC balance and source address is reduced by correct amount"
        amount == getbalance_MP(toAddress, MSC).balance
        endBalance == startBalance - amount
    }
}