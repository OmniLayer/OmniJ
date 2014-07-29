package com.msgilligan.bitcoin.rpc

import org.mastercoin.BaseRegTestSpec
import org.mastercoin.MPRegTestParams
import org.mastercoin.consensus.ConsensusComparison
import org.mastercoin.consensus.ConsensusTool
import org.mastercoin.consensus.MasterCoreConsensusTool
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

import java.security.SecureRandom

import static org.mastercoin.CurrencyID.MSC
import static org.mastercoin.CurrencyID.TMSC
import java.lang.Void as Should

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
        accountname = "msc" + new BigInteger(130, random).toString(32)
        wealthyAddress = getAccountAddress(accountname)
    }

    Should "Fund some MSC and TMSC"() {
        when: "we create a new account for Mastercoins and send some BTC to it"
        sendToAddress(wealthyAddress, 2*sendAmount + extraAmount)
        generateBlock()

        then: "we have the correct amount of BTC there"
        getBalance(accountname) >= 2*sendAmount + extraAmount

        when: "We send the BTC to the moneyManAddress and generate a block"
        def amounts = [(MPRegTestParams.MoneyManAddress): sendAmount, (MPRegTestParams.ExodusAddress): sendAmount]
        def txid = sendMany(accountname, amounts)
        generateBlock()
        def tx = getTransaction(txid)

        then: "transaction was confirmed"
        tx.confirmations == 1

        and: "The balances for the account we just sent MSC to is correct"
        getbalance_MP(wealthyAddress, MSC) == 100 * sendAmount
        getbalance_MP(wealthyAddress, TMSC) == 100 * sendAmount
    }

    @Ignore
    Should "Simple send TMSC from one address to another" () {

        when: "we send MSC"
        def senderBalance = getbalance_MP(wealthyAddress, MSC)
        def amount = 1.0
        def toAddress = getNewAddress()
        client.send_MP(wealthyAddress, toAddress, TMSC, amount)

        and: "a block is generated"
        generateBlocks(1)
        def newSenderBalance = getbalance_MP(wealthyAddress, TMSC)

        then: "the toAddress has the correct MSC balance and source address is reduced by right amount"
        newSenderBalance == senderBalance - amount
        getbalance_MP(toAddress, TMSC) == amount
    }

    @Ignore
    Should "Be able to send to owners (pay a dividend)"() {
        when: "We Send to Owners"
        cliSend("sendtoowners_MP", wealthyAddress, MSC as Integer, 1.0)

        then: "We're a little poorer"
        getbalance_MP(wealthyAddress, MSC) <= 100 * sendAmount
    }


}