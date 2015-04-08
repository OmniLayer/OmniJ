package foundation.omni.test.tx

import com.msgilligan.bitcoin.BTC
import foundation.omni.BaseRegTestSpec
import foundation.omni.tx.OmniTxBuilder
import org.bitcoinj.core.TransactionOutput

import static foundation.omni.CurrencyID.MSC

/**
 * Test OmniTXBuilder simple send using sendRawTransaction
 */
class OmniTxBuilderIntegSpec extends BaseRegTestSpec {
    static final BigDecimal startBTC = 10.0
    static final BigDecimal startMSC = 1000.0
    static final omniTxBuilder = new OmniTxBuilder()

    def "Can simple send amount MSC from one address to another using OmniTxBuilder and sendraw RPC"() {
        given: "a fundedAddress with BTC/MSC and a newly created toAddress"
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def startBalance = getbalance_MP(fundedAddress, MSC).balance
        def fundedKey = dumpPrivKey(fundedAddress)
        def toAddress = getNewAddress()
        List<TransactionOutput> utxos = listUnspentJ(fundedAddress)
        BigDecimal amount = 0.1

        when: "we build a signed Omni Simple Send Transaction in a bitcoinj Transaction object"
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