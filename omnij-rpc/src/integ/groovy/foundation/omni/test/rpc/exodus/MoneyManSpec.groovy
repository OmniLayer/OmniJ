package foundation.omni.test.rpc.exodus

import foundation.omni.OmniDivisibleValue
import foundation.omni.net.MoneyMan
import org.bitcoinj.base.Address
import foundation.omni.BaseRegTestSpec
import foundation.omni.rpc.OmniClient
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Sha256Hash
import spock.lang.Shared
import spock.lang.Stepwise

import static foundation.omni.CurrencyID.OMNI
import static foundation.omni.CurrencyID.TOMNI

@Stepwise
class MoneyManSpec extends BaseRegTestSpec {

    final static Coin sendAmount = 10.btc
    final static Coin extraAmount = 0.10.btc
    final static Coin faucetBTC = 10.btc
    final static OmniDivisibleValue faucetMSC = 1000.divisible
    final static OmniDivisibleValue simpleSendAmount = 1.divisible

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
        generateBlocks(1)

        then: "we have the correct amount of BTC in faucetAddress's account"
        getBitcoinBalance(testAddress) == sendAmount + extraAmount + stdTxFee

        when: "We send the BTC to the moneyManAddress and generate a block"
        txid = sendBitcoin(testAddress, omniNetParams.moneyManAddress, sendAmount)
        generateBlocks(1)
        def tx = client.getTransaction(txid)

        then: "transaction was confirmed"
        tx.confirmations == 1

        and: "The balances for the account we just sent OMNI to is correct"
        getBitcoinBalance(testAddress) == extraAmount
        omniGetBalance(testAddress, OMNI).balance ==  MoneyMan.toOmni(sendAmount).numberValue()
        omniGetBalance(testAddress, TOMNI).balance == MoneyMan.toOmni(sendAmount).numberValue()
    }

    def "check Spec setup"() {
        // This test is really an integration test of createFundedAddress()
        expect:
        getBitcoinBalance(faucetAddress) == faucetBTC
        omniGetBalance(faucetAddress, OMNI).balance == MoneyMan.toOmni(sendAmount).numberValue()
        omniGetBalance(faucetAddress, TOMNI).balance == MoneyMan.toOmni(sendAmount).numberValue()
    }

    def "Simple send MSC from one address to another" () {
        // This test either duplicates or should be moved to MSCSimpleSendSpec

        when: "we send OMNI"
        def senderBalance = omniGetBalance(faucetAddress, OMNI)
        def toAddress = getNewAddress()
        def txid = client.omniSend(faucetAddress, toAddress, OMNI, simpleSendAmount)
        def tx = client.getTransaction(txid)

        then: "we got a non-zero transaction id"
        txid != Sha256Hash.ZERO_HASH
        tx

        when: "a block is generated"
        generateBlocks(1)
        def newSenderBalance = client.omniGetBalance(faucetAddress, OMNI)
        def receiverBalance = client.omniGetBalance(toAddress, OMNI)

        then: "the toAddress has the correct OMNI balance and source address is reduced by right amount"
        receiverBalance.balance == simpleSendAmount.numberValue()
        newSenderBalance.balance == senderBalance.balance - simpleSendAmount.numberValue()
    }

    def "Send MSC back to same adddress" () {
        // This test either duplicates or should be moved to MSCSimpleSendSpec

        when: "we send OMNI"
        def wealthyBalance = omniGetBalance(faucetAddress, OMNI).balance
        def txid = omniSend(faucetAddress, faucetAddress, OMNI, 10.12345678.divisible)

        then: "we got a non-zero transaction id"
        txid != Sha256Hash.ZERO_HASH

        when: "a block is generated"
        generateBlocks(1)
        def newWealthyBalance = omniGetBalance(faucetAddress, OMNI).balance

        then: "balance is unchanged"
        newWealthyBalance == wealthyBalance
    }
}