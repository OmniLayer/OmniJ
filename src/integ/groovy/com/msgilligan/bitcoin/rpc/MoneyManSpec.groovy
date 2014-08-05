package com.msgilligan.bitcoin.rpc

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.Transaction
import com.msgilligan.bitcoin.BTC
import org.mastercoin.BaseRegTestSpec
import org.mastercoin.MPRegTestParams
import org.mastercoin.consensus.MasterCoreConsensusTool
import org.mastercoin.rpc.MastercoinClient
import spock.lang.Ignore
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
    String faucetAccount

    @Shared
    Address faucetAddress

    @Shared
    def consensusTool

    @Shared
    def consensusComparison


    def setupSpec() {
        faucetAccount = createNewAccount()
        faucetAddress = createFaucetAddress(faucetAccount, faucetBTC, faucetMSC)
        consensusTool = new MasterCoreConsensusTool(client)
    }

    @Ignore
    def "Send BTC to an address to get MSC and TMSC"() {
        when: "we create a new account for Mastercoins and send some BTC to it"
        def txid = sendToAddress(faucetAddress, sendAmount + extraAmount)

        and: "we generate a block"
        generateBlock()

        then: "we have the correct amount of BTC in faucetAddress's account"
        getBalance(faucetAccount) == sendAmount + extraAmount

        when: "We send the BTC to the moneyManAddress and generate a block"
        def amounts = [(MPRegTestParams.MoneyManAddress): sendAmount,
                       (faucetAddress): extraAmount - stdTxFee ]
        txid = sendMany(faucetAccount, amounts)
        generateBlock()
        def tx = client.getTransaction(txid)
//        def txmp = client.getTransactionMP(txid)

        then: "transaction was confirmed"
        tx.confirmations == 1
//        txmp.confirmations == 1

        and: "The balances for the account we just sent MSC to is correct"
//        getBalance(faucetAccount) == extraAmount - stdTxFee
        getbalance_MP(faucetAddress, MSC) == initalMSCPerBTC * sendAmount
        getbalance_MP(faucetAddress, TMSC) == initalMSCPerBTC * sendAmount
    }

    def "check Spec setup"() {
        expect:
        getBalance(faucetAccount) == faucetBTC
        getbalance_MP(faucetAddress, MSC) == initalMSCPerBTC * sendAmount
        getbalance_MP(faucetAddress, TMSC) == initalMSCPerBTC * sendAmount
    }

    def "Simple send MSC from one address to another" () {

        when: "we send MSC"
        def senderBalance = getbalance_MP(faucetAddress, MSC)
        def toAddress = getNewAddress()
        def txid = client.send_MP(faucetAddress, toAddress, MSC, simpleSendAmount)
        def tx = client.getTransaction(txid)

        then: "we got a non-zero transaction id"
        txid != MastercoinClient.zeroHash
        tx

        when: "a block is generated"
        generateBlocks(1)
        def newSenderBalance = client.getbalance_MP(faucetAddress, MSC)
        def receiverBalance = client.getbalance_MP(toAddress, MSC)
//        def tx = client.getTransaction(txid)

//        then: "the transaction is confirmed and valid"
//        tx.confirmations == 10

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
        receiverBalance == simpleSendAmount
        newSenderBalance == senderBalance - simpleSendAmount
    }

    def "Send MSC back to same adddress" () {

        when: "we send MSC"
        def wealthyBalance = getbalance_MP(faucetAddress, MSC)
        def txid = send_MP(faucetAddress, faucetAddress, MSC, 10.12345678)

        then: "we got a non-zero transaction id"
        txid != MastercoinClient.zeroHash

        when: "a block is generated"
        generateBlock()
        def newWealthyBalance = getbalance_MP(faucetAddress, MSC)

        then: "balance is unchanged"
        newWealthyBalance == wealthyBalance
    }

    def "Be able to send to owners"() {
        when: "We Send to Owners"
        def senderBalance = getbalance_MP(faucetAddress, TMSC)
//        ConsensusSnapshot snap1 = consensusTool.getConsensusSnapshot(TMSC)
        def txid = sendToOwnersMP(faucetAddress, TMSC, 100.00)
        generateBlock()
        def tx = client.getTransaction(txid)
        def balances = getallbalancesforid_MP(TMSC)
        def numberDest = balances.size() - 1
        def sendToOwnersFee = numberDest * 0.00000001
//        ConsensusSnapshot snap2 = consensusTool.getConsensusSnapshot(TMSC)
//        consensusComparison = new ConsensusComparison(snap1, snap2)

        then: "Our balance is a little lower since we're not getting back all coins we sent"
        getbalance_MP(faucetAddress, TMSC) == senderBalance - 100.0 - sendToOwnersFee
    }

//    @Unroll
//    def "#address #entry1 == #entry2"() {
//        expect:
//        entry1 == entry2
//
//        where:
//        [address, entry1, entry2] << consensusComparison
//    }
}