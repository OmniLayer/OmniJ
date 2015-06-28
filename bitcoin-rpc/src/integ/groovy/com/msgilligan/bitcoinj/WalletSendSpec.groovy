package com.msgilligan.bitcoinj

import com.msgilligan.bitcoin.rpc.JsonRPCStatusException
import org.bitcoinj.core.Address
import org.bitcoinj.core.BlockChain
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.PeerGroup
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.Wallet
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.store.MemoryBlockStore
import org.bitcoinj.utils.BriefLogFormatter
import com.msgilligan.bitcoin.BTC
import com.msgilligan.bitcoin.BaseRegTestSpec
import org.bitcoinj.wallet.AllowUnconfirmedCoinSelector
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Various interoperability tests between RPC server and bitcoinj wallets.
 */
@Ignore("'Send mined coins' intermittently fails because transaction is still pending")
@Stepwise
class WalletSendSpec extends BaseRegTestSpec {
    @Shared
    NetworkParameters params
    @Shared
    Wallet wallet
    @Shared
    PeerGroup peerGroup


    void setupSpec() {
//        BriefLogFormatter.initVerbose();
        BriefLogFormatter.initWithSilentBitcoinJ();
        params = RegTestParams.get()

        wallet = new Wallet(params)
        wallet.setCoinSelector(new AllowUnconfirmedCoinSelector())
        def store = new MemoryBlockStore(params)
        def chain = new BlockChain(params,wallet,store)
        peerGroup = new PeerGroup(params, chain)
        peerGroup.addWallet(wallet)
        peerGroup.start()
    }

    def "Send mined coins to fund a new BitcoinJ wallet"() {
        given:
        def fundingAddress = getNewAddress()
        def walletAddr = getNewAddress()
        def walletKey = dumpPrivKey(walletAddr)
        wallet.importKey(walletKey)
        def fundingAmount = 20.0
        requestBitcoin(fundingAddress, fundingAmount)
        def amount = 10.0

        when: "we send coins to the wallet and write a block"
        client.sendToAddress(walletAddr, amount)
        client.generateBlock()
        Integer walletHeight, rpcHeight
        while ( (walletHeight = wallet.getLastBlockSeenHeight()) < (rpcHeight = client.getBlockCount()) ) {
            // TODO: Figure out a way to do this without polling and sleeping
            println "WalletHeight < rpcHeight: ${walletHeight} < ${rpcHeight} -- Waiting..."
            Thread.sleep(100)
        }
        println "WalletHeight: ${walletHeight} == RPC Height: ${rpcHeight}"
        // Is it safe to assume that if walletHeight == rpcHeight then our transaction has been processed?

        then: "the coins arrive"
        client.getReceivedByAddress(walletAddr) == amount
        wallet.getBalance().longValue() == BTC.btcToSatoshis(amount).longValue()
    }

    def "Send from BitcoinJ wallet to the Bitcoin Core wallet"() {
        when: "we send coins from BitcoinJ and write a block"
        BigDecimal startAmount = 10.0
        BigDecimal amount = 1.0
        Address rpcAddress = getNewAddress()
        // Send it with BitcoinJ
        Wallet.SendResult sendResult = wallet.sendCoins(peerGroup,rpcAddress,BTC.btcToCoin(amount))
        // Wait for broadcast complete
        Transaction sentTx = sendResult.broadcastComplete.get()
        // Wait for it to show up on server as unconfirmed
        waitForUnconfirmedTransaction(sentTx.hash)
        // Once server has pending transaction, generate a block
        generateBlock()
        // Wait for wallet to get confirmation of the transaction
        sentTx.getConfidence().getDepthFuture(1).get()

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount
        wallet.getBalance().longValue() == BTC.btcToSatoshis(startAmount) - BTC.btcToSatoshis(amount) - Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.longValue()
    }

    def "create and send a transaction from BitcoinJ using wallet.completeTx"() {
        when:
        BigDecimal amount = 1.0
        def rpcAddress = getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(BTC.btcToCoin(amount), rpcAddress)
        Wallet.SendRequest request = Wallet.SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        Transaction sentTx = peerGroup.broadcastTransaction(request.tx).get();
        // Wait for it to show up on server as unconfirmed
        waitForUnconfirmedTransaction(sentTx.hash)
        generateBlock()

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount  // Verify rpcAddress balance
    }

    def "create a raw transaction using BitcoinJ but send with an RPC"() {
        when:
        BigDecimal amount = 1.0
        def rpcAddress = getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(BTC.btcToCoin(amount), rpcAddress)
        Wallet.SendRequest request = Wallet.SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        def txid = client.sendRawTransaction(tx)
        generateBlock()
        def confirmedTx = getTransaction(txid)

        then: "the transaction is confirmed"
        confirmedTx.confirmations == 1

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount  // Verify rpcAddress balance
    }

    /**
     * Wait for a transaction to show up on the server (as unconfirmed)
     * (e.g. to make sure Transaction if fully received before generating a block)
     *
     * @param txid Transaction ID (hash) of transaction we're waiting for
     */
    void waitForUnconfirmedTransaction(Sha256Hash txid) {
        Transaction pendingTx = null;
        while (pendingTx == null) {
            try {
                pendingTx = getRawTransaction(txid)
            } catch (JsonRPCStatusException e) {
                if (e.message != "No information available about transaction") {
                    throw e;
                }
            }
        }
    }
}
