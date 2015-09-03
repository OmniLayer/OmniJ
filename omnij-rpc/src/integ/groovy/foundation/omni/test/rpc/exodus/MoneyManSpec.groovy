package foundation.omni.test.rpc.exodus

import org.bitcoinj.core.Address
import foundation.omni.BaseRegTestSpec
import foundation.omni.net.OmniRegTestParams
import foundation.omni.rpc.OmniClient
import spock.lang.Shared
import spock.lang.Stepwise

import static foundation.omni.CurrencyID.MSC
import static foundation.omni.CurrencyID.TMSC

@Stepwise
class MoneyManSpec extends BaseRegTestSpec {

    final static BigDecimal sendAmount = 10.0
    final static BigDecimal extraAmount = 0.10
    final static BigDecimal faucetBTC = 10.0
    final static BigDecimal faucetMSC = 1000.0
    final static BigDecimal initialMSCPerBTC = 100.0
    final static BigDecimal simpleSendAmount = 1.0

    @Shared
    Address faucetAddress

    def setup() {
        faucetAddress = createFundedAddress(faucetBTC, faucetMSC)
    }

    def "Send BTC to an address to get MSC and TMSC"() {
        // This test is truly a test of MoneyMan functionality
        when: "we create a new address for Omni and send some BTC to it"
        Address testAddress = getNewAddress()
        def txid = sendToAddress(testAddress, sendAmount + extraAmount + stdTxFee)

        and: "we generate a block"
        generateBlock()

        then: "we have the correct amount of BTC in faucetAddress's account"
        getBitcoinBalance(testAddress) == sendAmount + extraAmount + stdTxFee

        when: "We send the BTC to the moneyManAddress and generate a block"
        txid = sendBitcoin(testAddress, OmniRegTestParams.get().moneyManAddress, sendAmount)
        generateBlock()
        def tx = client.getTransaction(txid)

        then: "transaction was confirmed"
        tx.confirmations == 1

        and: "The balances for the account we just sent MSC to is correct"
        getBitcoinBalance(testAddress) == extraAmount
        omniGetBalance(testAddress, MSC).balance == initialMSCPerBTC * sendAmount
        omniGetBalance(testAddress, TMSC).balance == initialMSCPerBTC * sendAmount
    }

    def "check Spec setup"() {
        // This test is really an integration test of createFundedAddress()
        expect:
        getBitcoinBalance(faucetAddress) == faucetBTC
        omniGetBalance(faucetAddress, MSC).balance == initialMSCPerBTC * sendAmount
        omniGetBalance(faucetAddress, TMSC).balance == initialMSCPerBTC * sendAmount
    }

    def "Simple send MSC from one address to another" () {
        // This test either duplicates or should be moved to MSCSimpleSendSpec

        when: "we send MSC"
        def senderBalance = omniGetBalance(faucetAddress, MSC)
        def toAddress = getNewAddress()
        def txid = client.omniSend(faucetAddress, toAddress, MSC, simpleSendAmount)
        def tx = client.getTransaction(txid)

        then: "we got a non-zero transaction id"
        txid != OmniClient.zeroHash
        tx

        when: "a block is generated"
        generateBlock()
        def newSenderBalance = client.omniGetBalance(faucetAddress, MSC)
        def receiverBalance = client.omniGetBalance(toAddress, MSC)

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
        receiverBalance.balance == simpleSendAmount
        newSenderBalance.balance == senderBalance.balance - simpleSendAmount
    }

    def "Send MSC back to same adddress" () {
        // This test either duplicates or should be moved to MSCSimpleSendSpec

        when: "we send MSC"
        def wealthyBalance = omniGetBalance(faucetAddress, MSC).balance
        def txid = omniSend(faucetAddress, faucetAddress, MSC, 10.12345678)

        then: "we got a non-zero transaction id"
        txid != OmniClient.zeroHash

        when: "a block is generated"
        generateBlock()
        def newWealthyBalance = omniGetBalance(faucetAddress, MSC).balance

        then: "balance is unchanged"
        newWealthyBalance == wealthyBalance
    }
}