package com.msgilligan.bitcoinj

import org.bitcoinj.core.Address
import org.bitcoinj.core.BlockChain
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.PeerAddress
import org.bitcoinj.core.PeerGroup
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.Wallet
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.store.MemoryBlockStore
import org.bitcoinj.utils.BriefLogFormatter
import com.msgilligan.bitcoin.BTC
import com.msgilligan.bitcoin.BaseRegTestSpec
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * User: sean
 * Date: 7/15/14
 * Time: 9:48 PM
 */
@Stepwise
class WalletSendSpec extends BaseRegTestSpec {
    @Shared
    NetworkParameters params
    @Shared
    Wallet wallet
    @Shared
    Address walletAddr
    @Shared
    MemoryBlockStore store
    @Shared
    BlockChain chain
    @Shared
    PeerGroup peerGroup


    void setupSpec() {
        BriefLogFormatter.initVerbose();
        params = RegTestParams.get()
        wallet = new Wallet(params)
        wallet.addKey(new ECKey())
        def keys = wallet.getKeys()
        ECKey key = keys.get(0)
        walletAddr = key.toAddress(params)

        store = new MemoryBlockStore(params)
        chain = new BlockChain(params,wallet,store)
        peerGroup = new PeerGroup(params, chain)
        peerGroup.addAddress(new PeerAddress(InetAddress.getLocalHost(), params.port))
        peerGroup.addWallet(wallet)
        peerGroup.startAndWait()
    }

    def "Send mined coins to fund a new BitcoinJ wallet"() {
        when: "we send coins to the wallet and write a block"
        BigDecimal amount = 20.0
        String txid = client.sendToAddress(walletAddr, amount, "fund BitcoinJ wallet", "first BitcoinJ wallet addr")
//        byte[] rawTx = client.getRawTransaction(txid, null)
//        Transaction tx = new Transaction(params, rawTx)
        Integer lastHeight = wallet.getLastBlockSeenHeight()
        client.generateBlock()
        while (wallet.lastBlockSeenHeight == lastHeight) {
            println "Waiting..."
            Thread.sleep(100)
        }
        Thread.sleep(400)   // Wait for BitcoinJ wallet to process block?

        then: "the coins arrive"
        wallet.balance == BTC.btcToSatoshis(amount)
    }

    def "Send from BitcoinJ wallet to the Bitcoind/Mastercore wallet"() {
        when: "we send coins from BitcoinJ and write a block"
        BigDecimal amount = 1.0
        def rpcAddress = getNewAddress()
        wallet.sendCoins(peerGroup,rpcAddress,BTC.btcToSatoshis(amount))
        Thread.sleep(400)            // Give BitcoinJ time to send Tx to bitcoind
        generateBlocks(2)

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount
        wallet.getBalance() == BTC.btcToSatoshis(20.0) - BTC.btcToSatoshis(1.0) - Transaction.REFERENCE_DEFAULT_MIN_TX_FEE
    }

    def "create and send a transaction from BitcoinJ using wallet.completeTx"() {
        given:
        wallet.getBalance() == BTC.btcToSatoshis(20.0) - BTC.btcToSatoshis(1.0) - Transaction.REFERENCE_DEFAULT_MIN_TX_FEE

        when:
        BigDecimal amount = 1.0
        def rpcAddress = getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(BTC.btcToSatoshis(amount), rpcAddress)
        Wallet.SendRequest request = Wallet.SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        peerGroup.broadcastTransaction(request.tx).get();
        Thread.sleep(100)            // Give bitcoind a little time to receive the Tx
        generateBlocks(1)

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount  // Verify rpcAddress balance
    }

    def "create a raw transaction using BitcoinJ but send with an RPC"() {
        given:
        wallet.getBalance() == BTC.btcToSatoshis(20.0) - BTC.btcToSatoshis(1.0) - BTC.btcToSatoshis(1.0) - 2 * Transaction.REFERENCE_DEFAULT_MIN_TX_FEE

        when:
        BigDecimal amount = 1.0
        def rpcAddress = getNewAddress()
        Transaction tx = new Transaction(params)
        tx.addOutput(BTC.btcToSatoshis(amount), rpcAddress)
        Wallet.SendRequest request = Wallet.SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        def txid = client.sendRawTransaction(tx)
        generateBlocks(1)
        def confirmedTx = getTransaction(txid)

        then: "the transaction is confirmed"
        confirmedTx.confirmations == 1

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount  // Verify rpcAddress balance
    }

}
