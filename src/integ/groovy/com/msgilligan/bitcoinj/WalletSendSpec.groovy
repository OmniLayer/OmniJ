package com.msgilligan.bitcoinj

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.BlockChain
import com.google.bitcoin.core.ECKey
import com.google.bitcoin.core.NetworkParameters
import com.google.bitcoin.core.PeerAddress
import com.google.bitcoin.core.PeerGroup
import com.google.bitcoin.core.Transaction
import com.google.bitcoin.core.TransactionOutput
import com.google.bitcoin.core.Wallet
import com.google.bitcoin.params.RegTestParams
import com.google.bitcoin.store.MemoryBlockStore
import com.google.bitcoin.utils.BriefLogFormatter
import com.google.bitcoin.utils.Threading
import com.msgilligan.bitcoin.BTC
import org.mastercoin.BaseRegTestSpec
import spock.lang.Shared
import spock.lang.Stepwise

import java.lang.Void as Should

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

    Should "Send mined coins to fund a new BitcoinJ wallet"() {
        when: "we send coins to the wallet and write a block"
        BigDecimal amount = 20.0
        String txid = client.sendToAddress(walletAddr, amount, "fund BitcoinJ wallet", "first BitcoinJ wallet addr")
//        byte[] rawTx = client.getRawTransaction(txid, null)
//        Transaction tx = new Transaction(params, rawTx)
        Integer lastHeight = wallet.getLastBlockSeenHeight()
        client.setGenerate(true, 1)                             // Generate 1 block
        while (wallet.lastBlockSeenHeight == lastHeight) {
            println "Waiting..."
            Thread.sleep(100)
        }
        Thread.sleep(400)   // Wait for BitcoinJ wallet to process block?

        then: "the coins arrive"
        wallet.balance == BTC.btcToSatoshis(amount)
    }

    Should "Send from BitcoinJ wallet to the Bitcoind/Mastercore wallet"() {
        when: "we send coins from BitcoinJ and write a block"
        BigDecimal amount = 1.0
        def rpcAddress = getNewAddress()
        wallet.sendCoins(peerGroup,rpcAddress,BTC.btcToSatoshis(amount))
        Thread.sleep(100)            // Give BitcoinJ time to send Tx to bitcoind
        generateBlocks(1)

        then: "the new address has a balance of amount"
        getReceivedByAddress(rpcAddress) == amount
    }

    Should "create and send a transaction from BitcoinJ using wallet.completeTx"() {
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
}
