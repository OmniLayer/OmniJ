package com.msgilligan.bitcoin.rpc

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

    @Shared
    def accountname

    @Shared
    def wealthyAddress

    def setupSpec() {
        // Create a new, unique address in a dedicated account
        def random = new SecureRandom();
        accountname = "msc-" + new BigInteger(130, random).toString(32)
        wealthyAddress = getAccountAddress(accountname)
    }

    def "Send BTC to an address to get MSC and TMSC"() {
        when: "we create a new account for Mastercoins and send some BTC to it"
        def txid = client.sendToAddress(wealthyAddress, 2*sendAmount + extraAmount)

        then: "we got a non-zero transaction id"
        txid != MastercoinClient.zeroHash

        when: "we generate a block"
        generateBlocks(1)

        then: "we have the correct amount of BTC there"
        getBalance(accountname) >= 2*sendAmount + extraAmount

        when: "We send the BTC to the moneyManAddress and generate a block"
        def amounts = [(MPRegTestParams.MoneyManAddress): sendAmount, (MPRegTestParams.ExodusAddress): sendAmount]
        txid = sendMany(accountname, amounts)
        generateBlock()
        def tx = getTransaction(txid)

        then: "transaction was confirmed"
        tx.confirmations == 1

        and: "The balances for the account we just sent MSC to is correct"
        getbalance_MP(wealthyAddress, MSC) == 100 * sendAmount
        getbalance_MP(wealthyAddress, TMSC) == 100 * sendAmount
    }

    def "Simple send MSC from one address to another" () {

        when: "we send MSC"
        // TODO: Find the change address or send change back to original address
        def senderBalance = getbalance_MP(wealthyAddress, MSC)
        def amount = 1.0
        def toAddress = getNewAddress()
        def txid = client.send_MP(wealthyAddress, toAddress, MSC, amount)

        then: "we got a non-zero transaction id"
        txid != MastercoinClient.zeroHash

        when: "a block is generated"
        generateBlocks(10)
        def newSenderBalance = getbalance_MP(wealthyAddress, MSC)
        def receiverBalance = getbalance_MP(toAddress, MSC)
//        def tx = client.getTransaction(txid)

//        then: "the transaction is confirmed and valid"
//        tx.confirmations == 10

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
        newSenderBalance == senderBalance - amount
        receiverBalance == amount
    }

    @Ignore
    def "Be able to send to owners (pay a dividend)"() {
        when: "We Send to Owners"
        cliSend("sendtoowners_MP", wealthyAddress, MSC as Integer, 1.0)

        then: "We're a little poorer"
        getbalance_MP(wealthyAddress, MSC) <= 100 * sendAmount
    }


}