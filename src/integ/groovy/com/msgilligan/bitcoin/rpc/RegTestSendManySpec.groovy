package com.msgilligan.bitcoin.rpc

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.ECKey
import com.google.bitcoin.core.NetworkParameters
import com.google.bitcoin.params.RegTestParams
import org.mastercoin.BaseRegTestSpec
import org.mastercoin.MPRegTestParams
import spock.lang.Specification

import java.security.SecureRandom

import static org.mastercoin.CurrencyID.MSC
import static org.mastercoin.CurrencyID.MSC
import static org.mastercoin.CurrencyID.TMSC
import static org.mastercoin.CurrencyID.TMSC


/**
 * Demonstrate possible Bitcoin Core bug detected while
 * writing Master Core tests.
 */
class RegTestSendManySpec extends BaseRegTestSpec {
    final static BigDecimal sendAmount = 10.0
    final static BigDecimal extraAmount = 1.0

    def "Send BTC from a newly created address and wallet and make sure sending address is correct"() {
        given: "A newly created RPC account and a newly created BTC address"
        String namedAccount = new BigInteger(130, new SecureRandom()).toString(32)
        Address newAddress = getAccountAddress(namedAccount)
        NetworkParameters params = RegTestParams.get()
        Address externalAddress = new ECKey().toAddress(params)


        when: "we send some BTC to new address from default account to newAccount/newAddress"
        def txid = sendToAddress(newAddress, sendAmount + extraAmount + stdTxFee)

        and: "we generate a block"
        generateBlock()
        def tx = client.getTransaction(txid)
        def namedAccountBalance = getBalance(namedAccount)

        then: "transaction is confirmed"
        tx.confirmations == 1

        and: "we have the correct amount of BTC in namedAccount"
        namedAccountBalance == sendAmount + extraAmount + stdTxFee

        when: "We send BTC to the externalAddress from the named account"
        def amounts = [(externalAddress): sendAmount,
                       (newAddress): extraAmount ]  // Send change back to ourselves
        txid = sendMany(namedAccount, amounts)

        and: "We generate a block"
        generateBlock()
        tx = client.getTransaction(txid)
        def jtx = client.getRawTransaction(txid)

        def sourceAddr = jtx.getInput(0).getFromAddress()
//        def inputConnectedOutput = jtx.getInput(0).getConnectedOutput()
        def btcBalance = getBalance(namedAccount)
        def btcReceived = getReceivedByAddress(newAddress)

        then: "transaction was confirmed"
        tx.confirmations == 1

        and: "The balance in named account is as expected"
        btcBalance == extraAmount

        and: "The transaction source address was newAddress, the only address in namedAccount"
        sourceAddr == newAddress

    }

}