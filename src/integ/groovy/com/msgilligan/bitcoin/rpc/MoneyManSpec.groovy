package com.msgilligan.bitcoin.rpc

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.Transaction
import com.msgilligan.bitcoin.BTC
import org.mastercoin.BaseRegTestSpec
import org.mastercoin.MPRegTestParams
import org.mastercoin.consensus.ConsensusComparison
import org.mastercoin.consensus.ConsensusTool
import org.mastercoin.consensus.MasterCoreConsensusTool
import org.mastercoin.rpc.MastercoinClient
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

import java.security.SecureRandom

import static org.mastercoin.CurrencyID.MSC
import static org.mastercoin.CurrencyID.TMSC

@Stepwise
class MoneyManSpec extends BaseRegTestSpec {

    final static BigDecimal sendAmount = 10.0
    final static BigDecimal extraAmount = 0.10
    final static BigDecimal initalMSCPerBTC = 100.0
    final static BigDecimal simpleSendAmount = 1.0
    final static BigDecimal stdTxFee = BTC.satoshisToBTC(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE)

    @Shared
    String accountname

    @Shared
    Address wealthyAddress

    def setupSpec() {
        // Create a new, unique address in a dedicated account
        def random = new SecureRandom();
        accountname = "msc-" + new BigInteger(130, random).toString(32)
        wealthyAddress = getAccountAddress(accountname)
    }

    def "Send BTC to an address to get MSC and TMSC"() {
        when: "we create a new account for Mastercoins and send some BTC to it"
        def txid = sendToAddress(wealthyAddress, 2*sendAmount + extraAmount)

        and: "we generate a block"
        generateBlocks(1)

        then: "we have the correct amount of BTC there"
        getBalance(accountname) == 2*sendAmount + extraAmount

        when: "We send the BTC to the moneyManAddress and generate a block"
        def amounts = [(MPRegTestParams.MoneyManAddress): sendAmount,
                       (MPRegTestParams.ExodusAddress): sendAmount,
                       (wealthyAddress): extraAmount - stdTxFee ]
        txid = sendMany(accountname, amounts)
        generateBlock()
        def tx = client.getTransaction(txid)
//        def txmp = client.getTransactionMP(txid)

        then: "transaction was confirmed"
        tx.confirmations == 1
//        txmp.confirmations == 1

        and: "The balances for the account we just sent MSC to is correct"
        getbalance_MP(wealthyAddress, MSC) == initalMSCPerBTC * sendAmount
        getbalance_MP(wealthyAddress, TMSC) == initalMSCPerBTC * sendAmount
    }

    def "Simple send MSC from one address to another" () {

        when: "we send MSC"
        def senderBalance = getbalance_MP(wealthyAddress, MSC)
        def toAddress = getNewAddress()
        def txid = client.send_MP(wealthyAddress, toAddress, MSC, simpleSendAmount)
        def tx = client.getTransaction(txid)

        then: "we got a non-zero transaction id"
        txid != MastercoinClient.zeroHash
        tx

        when: "a block is generated"
        generateBlocks(1)
        def newSenderBalance = client.getbalance_MP(wealthyAddress, MSC)
        def receiverBalance = client.getbalance_MP(toAddress, MSC)
//        def tx = client.getTransaction(txid)

//        then: "the transaction is confirmed and valid"
//        tx.confirmations == 10

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
        receiverBalance == simpleSendAmount
        newSenderBalance == senderBalance - simpleSendAmount
    }

    @Ignore
    def "Be able to send to owners"() {
        when: "We Send to Owners"
        def senderBalance = getbalance_MP(wealthyAddress, MSC)
        sendToOwnersMP(wealthyAddress, MSC, 500.00)

        then: "Our balance is a little lower since we're not getting back all coins we sent"
        getbalance_MP(wealthyAddress, MSC) < senderBalance
    }


}