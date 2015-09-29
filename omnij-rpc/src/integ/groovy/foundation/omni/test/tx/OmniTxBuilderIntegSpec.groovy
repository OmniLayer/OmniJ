package foundation.omni.test.tx

import foundation.omni.BaseRegTestSpec
import foundation.omni.OmniDivisibleValue
import foundation.omni.tx.OmniTxBuilder
import org.bitcoinj.core.Coin
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.params.RegTestParams

import static foundation.omni.CurrencyID.MSC

/**
 * Test OmniTXBuilder simple send using sendRawTransaction
 */
class OmniTxBuilderIntegSpec extends BaseRegTestSpec {
    static final Coin startBTC = 10.btc
    static final OmniDivisibleValue startMSC = 1000.divisible
    static final omniTxBuilder = new OmniTxBuilder(RegTestParams.get())

    def "Can simple send amount MSC from one address to another using OmniTxBuilder and sendraw RPC"() {
        given: "a fundedAddress with BTC/MSC and a newly created toAddress"
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def startBalance = omniGetBalance(fundedAddress, MSC).balance
        def fundedKey = dumpPrivKey(fundedAddress)
        def toAddress = getNewAddress()
        List<TransactionOutput> utxos = listUnspentJ(fundedAddress)
        def amount = 0.1.divisible

        when: "we build a signed Omni Simple Send Transaction in a bitcoinj Transaction object"
        def tx = omniTxBuilder.createSignedSimpleSend(fundedKey, utxos, toAddress, MSC, amount)

        and: "we send it"
        def txid = sendRawTransaction(tx)

        and: "a block is generated"
        generateBlock()
        def endBalance = omniGetBalance(fundedAddress, MSC).balance

        then: "the toAddress has the correct MSC balance and source address is reduced by correct amount"
        amount.numberValue() == omniGetBalance(toAddress, MSC).balance
        endBalance == startBalance - amount.numberValue()
    }
}