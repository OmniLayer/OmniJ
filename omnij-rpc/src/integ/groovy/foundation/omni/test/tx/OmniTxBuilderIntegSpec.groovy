package foundation.omni.test.tx

import foundation.omni.BaseRegTestSpec
import foundation.omni.OmniDivisibleValue
import foundation.omni.tx.OmniTxBuilder
import org.bitcoinj.core.Coin
import org.bitcoinj.core.TransactionOutput
import spock.lang.Shared

import static foundation.omni.CurrencyID.OMNI

/**
 * Test OmniTXBuilder simple send using sendRawTransaction
 */
class OmniTxBuilderIntegSpec extends BaseRegTestSpec {
    static final Coin startBTC = 10.btc
    static final OmniDivisibleValue startMSC = 1000.divisible

    @Shared
    def omniTxBuilder

    def setup() {
        omniTxBuilder = new OmniTxBuilder(netParams)
    }

    def "Can simple send amount MSC from one address to another using OmniTxBuilder and sendraw RPC"() {
        given: "a fundedAddress with BTC/OMNI and a newly created toAddress"
        def fundedAddress = createFundedAddress(startBTC, startMSC)
        def startBalance = omniGetBalance(fundedAddress, OMNI).balance
        def fundedKey = dumpPrivKey(fundedAddress)
        def toAddress = getNewAddress()
        List<TransactionOutput> utxos = listUnspentJ(fundedAddress)
        def amount = 0.1.divisible

        when: "we build a signed Omni Simple Send Transaction in a bitcoinj Transaction object"
        def tx = omniTxBuilder.createSignedSimpleSend(fundedKey, utxos, toAddress, OMNI, amount)

        and: "we send it"
        def txid = sendRawTransaction(tx)

        and: "a block is generated"
        generateBlock()
        def endBalance = omniGetBalance(fundedAddress, OMNI).balance

        then: "the toAddress has the correct OMNI balance and source address is reduced by correct amount"
        amount.numberValue() == omniGetBalance(toAddress, OMNI).balance
        endBalance == startBalance - amount.numberValue()
    }
}