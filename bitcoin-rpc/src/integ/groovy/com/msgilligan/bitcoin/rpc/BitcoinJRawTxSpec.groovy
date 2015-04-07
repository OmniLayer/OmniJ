package com.msgilligan.bitcoin.rpc

import com.msgilligan.bitcoin.BTC
import com.msgilligan.bitcoin.BaseRegTestSpec
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.params.RegTestParams
import spock.lang.Shared
import spock.lang.Stepwise


/**
 * Test building raw transactions with BitcoinJ and validate with RPC/RegTest
 *
 * Note: Original plan was to create an unsigned bitcoinj Transaction, then sign it,
 * then send it. The commented out code hints at this attempt. I hope to solve this later.
 *
 */
@Stepwise
class BitcoinJRawTxSpec extends BaseRegTestSpec {
    static final NetworkParameters netParams = RegTestParams.get()
    final static BigDecimal fundingAmount = 10.0
    final static BigDecimal sendingAmount = 1.0

    @Shared
    Address fundingAddress

    @Shared
    Address destinationAddress

    @Shared
    Transaction tx

//    @Shared
//    TransactionSigner signer;
//
//    @Shared
//    TestKeyBag keyBag;
//    KeyChainGroup keyBag;
//    KeyBag keyBag;

//    def setupSpec() {
//        keyBag = new TestKeyBag()
//        keyBag = new KeyChainGroup(netParams)
//        signer = new LocalTransactionSigner()
//    }

    def "Fund address as intermediate"() {
        when: "a new address is created"
        fundingAddress = getNewAddress()

        and: "coins are sent to the new address from a random source"
        sendToAddress(fundingAddress, fundingAmount)

        and: "a new block is mined"
        generateBlock()

        then: "the address should have that balance"
        def balance = getBitcoinBalance(fundingAddress)
        balance == fundingAmount
    }

    def "Create Signed raw transaction"() {
        given: "a newly created address as destination"
        destinationAddress = getNewAddress("destinationAddress")

        when: "we get the signing key from the server"
        def key = dumpPrivKey(fundingAddress)

        and: "we create an unsigned bitcoinj transaction, spending from fundingAddress to destinationAddress"
        tx = createSignedTransaction(key, destinationAddress, sendingAmount)

        then: "there should be a valid signed transaction"
        tx != null
        tx.outputs.size() > 0
        tx.inputs.size() > 0
    }

//    def "Sign unsigned raw transaction"() {
//        given: "the private key is in the keybag used by the signer"
//        def key = dumpPrivKey(fundingAddress)
////        keyBag.add(key)
////        keyBag.importKeys(key)
//        keyBag = Wallet.fromKeys(netParams, [key])
//
//        when: "the transaction is signed"
//        def proposedTx = new TransactionSigner.ProposedTransaction(tx);
//        def signed = signer.signInputs(proposedTx, keyBag)
//
//        then: "all inputs should be signed"
//        signed == true
//        tx.inputs.every { it.getScriptSig() != null }
//    }

    def "Broadcast signed raw transaction"() {
        when: "the transaction is sent"
        def txid = sendRawTransaction(tx)

        then: "there should be a transaction hash"
        txid != null

        when: "a new block is mined"
        generateBlock()

        then: "the transaction should have 1 confirmation"
        def broadcastedTransaction = getRawTransaction(txid, true)
        def confirmations = broadcastedTransaction["confirmations"]
        confirmations == 1

        and: "#fundingAddress has a remainder of coins minus transaction fees"
        def balanceRemaining = getBitcoinBalance(fundingAddress)
        balanceRemaining == fundingAmount - sendingAmount - stdTxFee

        and: "#destinationAddress has a balance matching the spent amount"
        def balanceDestination = getBitcoinBalance(destinationAddress)
        balanceDestination == sendingAmount
    }


    Transaction createSignedTransaction(ECKey fromKey, List<TransactionOutput> outputs) {
        Address fromAddress = fromKey.toAddress(netParams)
        Transaction tx = new Transaction(netParams)
        def unspentOutputs = listUnspent(0, defaultMaxConf, [fromAddress])

        // Add outputs to the transaction
        outputs.each {
            tx.addOutput(it)
        }

        // Calculate change
        BigDecimal amountIn     = unspentOutputs.sum { it.amount }
        BigDecimal amountOut    = outputs.sum { BTC.coinToBTC(it.value) }
        BigDecimal amountChange = amountIn - amountOut - stdTxFee
        if (amountIn < (amountOut + stdTxFee)) {
            println "Insufficient funds: ${amountIn} < ${amountOut + stdTxFee}"
        }
        if (amountChange > 0) {
            // Add a change output
            tx.addOutput(BTC.btcToCoin(amountChange), fromAddress)
        }

        // Add all UTXOs for fromAddress as inputs
        unspentOutputs.each {
            Transaction connectedTx = getRawTransaction(it.txid)
            TransactionOutput output = connectedTx.getOutput(it.vout)
            tx.addSignedInput(output, fromKey)
        }

        return tx;
    }

    Transaction createSignedTransaction(ECKey fromKey, Address toAddress, BigDecimal amount) {
        def outputs = [new TransactionOutput(netParams, null, BTC.btcToCoin(amount),toAddress)]
        return createSignedTransaction(fromKey, outputs)
    }

}