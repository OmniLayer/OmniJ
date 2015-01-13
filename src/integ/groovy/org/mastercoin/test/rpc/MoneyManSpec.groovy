package org.mastercoin.test.rpc

import com.google.bitcoin.core.Address
import org.mastercoin.BaseRegTestSpec
import org.mastercoin.MPRegTestParams
import org.mastercoin.consensus.MasterCoreConsensusTool
import org.mastercoin.rpc.MastercoinClient
import spock.lang.Shared
import spock.lang.Stepwise

import static org.mastercoin.CurrencyID.MSC
import static org.mastercoin.CurrencyID.TMSC

@Stepwise
class MoneyManSpec extends BaseRegTestSpec {

    final static BigDecimal sendAmount = 10.0
    final static BigDecimal extraAmount = 0.10
    final static BigDecimal faucetBTC = 10.0
    final static BigDecimal faucetMSC = 1000.0
    final static BigDecimal initalMSCPerBTC = 100.0
    final static BigDecimal simpleSendAmount = 1.0

    @Shared
    Address faucetAddress

    @Shared
    def consensusTool

    @Shared
    def consensusComparison


    def setupSpec() {
        faucetAddress = createFundedAddress(faucetBTC, faucetMSC)
        consensusTool = new MasterCoreConsensusTool(client)
    }

    def "Send BTC to an address to get MSC and TMSC"() {
        // This test is truly a test of MoneyMan functionality
        when: "we create a new address for Mastercoins and send some BTC to it"
        Address testAddress = getNewAddress()
        def txid = sendToAddress(testAddress, sendAmount + extraAmount + stdTxFee)

        and: "we generate a block"
        generateBlock()

        then: "we have the correct amount of BTC in faucetAddress's account"
        getBitcoinBalance(testAddress) == sendAmount + extraAmount + stdTxFee

        when: "We send the BTC to the moneyManAddress and generate a block"
        txid = sendBitcoin(testAddress, MPRegTestParams.get().moneyManAddress, sendAmount)
        generateBlock()
        def tx = client.getTransaction(txid)

        then: "transaction was confirmed"
        tx.confirmations == 1

        and: "The balances for the account we just sent MSC to is correct"
        getBitcoinBalance(testAddress) == extraAmount
        getbalance_MP(testAddress, MSC).balance == initalMSCPerBTC * sendAmount
        getbalance_MP(testAddress, TMSC).balance == initalMSCPerBTC * sendAmount
    }

    def "check Spec setup"() {
        // This test is really an integration test of createFundedAddress()
        expect:
        getBitcoinBalance(faucetAddress) == faucetBTC
        getbalance_MP(faucetAddress, MSC).balance == initalMSCPerBTC * sendAmount
        getbalance_MP(faucetAddress, TMSC).balance == initalMSCPerBTC * sendAmount
    }

    def "Simple send MSC from one address to another" () {
        // This test either duplicates or should be moved to MSCSimpleSendSpec

        when: "we send MSC"
        def senderBalance = getbalance_MP(faucetAddress, MSC)
        def toAddress = getNewAddress()
        def txid = client.send_MP(faucetAddress, toAddress, MSC, simpleSendAmount)
        def tx = client.getTransaction(txid)

        then: "we got a non-zero transaction id"
        txid != MastercoinClient.zeroHash
        tx

        when: "a block is generated"
        generateBlock()
        def newSenderBalance = client.getbalance_MP(faucetAddress, MSC)
        def receiverBalance = client.getbalance_MP(toAddress, MSC)

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
        receiverBalance.balance == simpleSendAmount
        newSenderBalance.balance == senderBalance.balance - simpleSendAmount
    }

    def "Send MSC back to same adddress" () {
        // This test either duplicates or should be moved to MSCSimpleSendSpec

        when: "we send MSC"
        def wealthyBalance = getbalance_MP(faucetAddress, MSC).balance
        def txid = send_MP(faucetAddress, faucetAddress, MSC, 10.12345678)

        then: "we got a non-zero transaction id"
        txid != MastercoinClient.zeroHash

        when: "a block is generated"
        generateBlock()
        def newWealthyBalance = getbalance_MP(faucetAddress, MSC).balance

        then: "balance is unchanged"
        newWealthyBalance == wealthyBalance
    }

    def "Be able to send to owners"() {
        // This test should be flushed out and moved to the MSCSendToOwnersSpec
        when: "We Send to Owners"
        def senderBalance = getbalance_MP(faucetAddress, TMSC).balance
//        ConsensusSnapshot snap1 = consensusTool.getConsensusSnapshot(TMSC)
        def txid = sendToOwnersMP(faucetAddress, TMSC, 100.00)
        generateBlock()
        def tx = client.getTransaction(txid)
        def balances = getallbalancesforid_MP(TMSC)
        def numberDest = balances.size() - 1
        def sendToOwnersFee = numberDest * 0.00000001
        def expectedBalance = (numberDest == 0) ? senderBalance : senderBalance - 100.0 - sendToOwnersFee
//        ConsensusSnapshot snap2 = consensusTool.getConsensusSnapshot(TMSC)
//        consensusComparison = new ConsensusComparison(snap1, snap2)

        then: "Our balance is a little lower since we're not getting back all coins we sent"
        getbalance_MP(faucetAddress, TMSC).balance == expectedBalance
    }
}