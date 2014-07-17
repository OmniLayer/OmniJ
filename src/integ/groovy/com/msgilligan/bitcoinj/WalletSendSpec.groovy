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
import com.msgilligan.bitcoin.rpc.BaseRPCSpec
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * User: sean
 * Date: 7/15/14
 * Time: 9:48 PM
 */
@Stepwise
class WalletSendSpec extends BaseRPCSpec {
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

    def "can send mined coins to fund a new BitcoinJ wallet"() {
        when: "we send coins to the wallet and write a block"
        BigDecimal amount = 20.0
        String txid = client.sendToAddress(walletAddr.toString(), amount, "fund BitcoinJ wallet", "first BitcoinJ wallet addr")
//        byte[] rawTx = client.getRawTransaction(txid, null)
//        Transaction tx = new Transaction(params, rawTx)
        Integer lastHeight = wallet.getLastBlockSeenHeight()
        client.setGenerate(true, 1)                             // Generate 1 block
        while(wallet.getLastBlockSeenHeight() == lastHeight) {
            println "Waiting..."
            Thread.sleep(100)
        }
        Thread.sleep(400)   // Wait for BitcoinJ wallet to process block?

        then: "the coins arrive"
        wallet.balance == BTC.btcToSatoshis(amount)
    }

    def "can send from BitcoinJ wallet to the RPC wallet"() {
        when: "we send coins from BitcoinJ and write a block"
        BigDecimal amount = 1.0
        String rpcAddressStr = client.getNewAddress()
        Address rpcAddress = new Address(params,rpcAddressStr)
        wallet.sendCoins(peerGroup,rpcAddress,BTC.btcToSatoshis(amount))
        Thread.sleep(100)            // Give BitcoinJ time to send Tx to bitcoind
        client.setGenerate(true, 1)  // Generate 1 block

        then: "the new address has a balance of amount"
        client.getReceivedByAddress(rpcAddressStr, 1) == amount  // Verify rpcAddress balance
    }

    def "create and send a transaction from BitcoinJ using wallet.completeTx"() {
        when:
        BigDecimal amount = 1.0
        String rpcAddressStr = client.getNewAddress()
        Address rpcAddress = new Address(params,rpcAddressStr)
        Transaction tx = new Transaction(params)
        tx.addOutput(BTC.btcToSatoshis(amount), rpcAddress)
        Wallet.SendRequest request = Wallet.SendRequest.forTx(tx)
        wallet.completeTx(request)  // Find an appropriate input, calculate fees, etc.
        wallet.commitTx(request.tx)
        peerGroup.broadcastTransaction(request.tx).get();
        Thread.sleep(100)            // Give bitcoind a little time to receive the Tx
        client.setGenerate(true, 1)  // Generate 1 block

        then: "the new address has a balance of amount"
        client.getReceivedByAddress(rpcAddressStr, 1) == amount  // Verify rpcAddress balance
    }
}
