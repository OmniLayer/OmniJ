package com.msgilligan.bitcoin.rpc

import com.google.bitcoin.core.ECKey
import com.google.bitcoin.params.RegTestParams
import foundation.omni.BaseRegTestSpec
import spock.lang.Ignore

import java.security.SecureRandom

/**
 * Demonstrate possible Bitcoin Core bug detected while
 * writing Master Core tests.
 *
 * Turns out this is a well-known issue with Bitcoin accounts. -- Test ignored for now.
 */
@Ignore
class RegTestSendManySpec extends BaseRegTestSpec {
    final static BigDecimal sendAmount = 10.0
    final static BigDecimal extraAmount = 1.0

    def "Send BTC from a newly created address and wallet and make sure sending address is correct"() {
        given: "A newly created RPC account and a newly created BTC address"
        def namedAccount = new BigInteger(130, new SecureRandom()).toString(32)
        def newAddress = getaccountaddress(namedAccount)
        def externalAddress = new ECKey().toAddress(RegTestParams.get())

        when: "we send some BTC to new address from default account to newAccount/newAddress"
        def txid = sendtoaddress(newAddress, sendAmount + extraAmount + stdTxFee)

        and: "we generate a block"
        setgenerate(true, 1)
        def tx = gettransaction(txid)
        def namedAccountBalance = getbalance(namedAccount)

        then: "transaction is confirmed"
        tx.confirmations == 1

        and: "we have the correct amount of BTC in namedAccount"
        namedAccountBalance == sendAmount + extraAmount + stdTxFee

        when: "We send BTC to the externalAddress from the named account"
        def amounts = [(externalAddress): sendAmount,
                       (newAddress): extraAmount ]  // Send change back to ourselves
        txid = sendmany(namedAccount, amounts)

        and: "We generate a block"
        setgenerate(true, 1)
        tx = gettransaction(txid)
        def txObj = getrawtransaction(txid)

        def sourceAddr = txObj.inputs[0].fromAddress
        def btcBalance = getbalance(namedAccount)

        then: "transaction was confirmed"
        tx.confirmations == 1

        and: "The transaction source address was newAddress, the only address in namedAccount"
        sourceAddr == newAddress

        and: "The balance in named account is as expected"
        btcBalance == extraAmount
    }

}