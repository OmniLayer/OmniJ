package com.msgilligan.bitcoin.test

import com.msgilligan.bitcoin.rpc.BitcoinClientDelegate
import com.msgilligan.bitcoin.rpc.Outpoint
import org.bitcoinj.core.Address
import org.bitcoinj.core.Sha256Hash

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait BTCTestSupport implements BitcoinClientDelegate {
    // TODO: set, or get and verify default values of the client
    final BigDecimal stdTxFee = new BigDecimal('0.00010000')
    final BigDecimal stdRelayTxFee = new BigDecimal('0.00001000')
    final Integer defaultMaxConf = 9999999

    /**
     * Generate blocks and fund an address with requested amount of BTC
     *
     * TODO: Improve performance. Can we mine multiple blocks with a single RPC?
     *
     * @param toAddress Address to fund with BTC
     * @param requestedBTC Amount of BTC to "mine" and send
     * @return
     */
    Sha256Hash requestBitcoin(Address toAddress, BigDecimal requestedBTC) {
        def amountGatheredSoFar = 0.0
        def inputs = new ArrayList<Outpoint>()

        // Newly mined coins need to mature to be spendable
        def minCoinAge = 100

        if (blockCount < minCoinAge) {
            generateBlocks(minCoinAge - blockCount)
        }

        while (amountGatheredSoFar < requestedBTC) {
            generateBlock()
            def blockIndex = blockCount - minCoinAge
            def block = client.getBlock(blockIndex)
            def blockTxs = block.tx as List<String>
            def coinbaseTx = new Sha256Hash(blockTxs.get(0))
            def txout = client.getTxOut(coinbaseTx, 0)

            // txout is empty, if output was already spent
            if (txout && txout.containsKey("value")) {
                def amountBTCd = txout.value as Double
                amountGatheredSoFar += BigDecimal.valueOf(amountBTCd)
                inputs << new Outpoint(coinbaseTx, 0)
            }
        }

        // Don't care about change, we mine it anyway
        def outputs = new HashMap<Address, BigDecimal>()
        outputs.put(toAddress, requestedBTC)

        def unsignedTxHex = client.createRawTransaction(inputs, outputs)
        def signingResult = client.signRawTransaction(unsignedTxHex)

        assert signingResult.complete == true

        def signedTxHex = signingResult.hex as String
        def txid = client.sendRawTransaction(signedTxHex, true)

        return txid
    }

    /**
     * Creates a raw transaction, spending from a single address, whereby no new change address is created, and
     * remaining amounts are returned to {@code fromAddress}.
     *
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param fromAddress The source to spent from
     * @param outputs The destinations and amounts to transfer
     * @return The hex-encoded raw transaction
     */
    String createRawTransaction(Address fromAddress, Map<Address, BigDecimal> outputs) {
        def amountIn = new BigDecimal(0)
        def amountOut = new BigDecimal(0)
        def inputs = new ArrayList<Outpoint>()
        def unspentOutputs = listUnspent(0, defaultMaxConf, [fromAddress])

        // Gather inputs
        for (unspentOutput in unspentOutputs) {
            def outpoint = new Outpoint(unspentOutput.txid, unspentOutput.vout)
            amountIn += unspentOutput.amount
            inputs.add(outpoint)
        }

        // Sum outgoing amount
        for (entry in outputs.entrySet()) {
            amountOut += entry.value
        }

        // Calculate change
        def amountChange = amountIn - amountOut - stdTxFee
        if (amountIn < (amountOut + stdTxFee)) {
            println "Insufficient funds: ${amountIn} < ${amountOut + stdTxFee}"
        }
        if (amountChange > 0) {
            outputs[fromAddress] = amountChange
        }

        return createRawTransaction(inputs, outputs)
    }

    /**
     * Creates a raw transaction, sending {@code amount} from a single address to a destination, whereby no new change
     * address is created, and remaining amounts are returned to {@code fromAddress}.
     *
     * Note: the transaction inputs are not signed, and the transaction is not stored in the wallet or transmitted to
     * the network.
     *
     * @param fromAddress The source to spent from
     * @param toAddress The destination
     * @param amount The amount
     * @return The hex-encoded raw transaction
     */
    String createRawTransaction(Address fromAddress, Address toAddress, BigDecimal amount) {
        def outputs = new HashMap<Address, BigDecimal>()
        outputs[toAddress] = amount
        return createRawTransaction(fromAddress, outputs)
    }



    /**
     * Returns the Bitcoin balance of an address.
     *
     * @param address The address
     * @return The balance
     */
    BigDecimal getBitcoinBalance(Address address) {
        // NOTE: because null is currently removed from the argument lists passed via RPC, using it here for default
        // values would result in the RPC call "listunspent" with arguments [["address"]], which is invalid, similar
        // to a call with arguments [null, null, ["address"]], as expected arguments are either [], [int], [int, int]
        // or [int, int, array]
        return getBitcoinBalance(address, 1, defaultMaxConf)
    }

    /**
     * Returns the Bitcoin balance of an address where spendable outputs have at least {@code minConf} confirmations.
     *
     * @param address The address
     * @param minConf Minimum amount of confirmations
     * @return The balance
     */
    BigDecimal getBitcoinBalance(Address address, Integer minConf) {
        return getBitcoinBalance(address, minConf, defaultMaxConf)
    }

    /**
     * Returns the Bitcoin balance of an address where spendable outputs have at least {@code minConf} and not more
     * than {@code maxConf} confirmations.
     *
     * @param address The address
     * @param minConf Minimum amount of confirmations
     * @param maxConf Maximum amount of confirmations
     * @return The balance
     */
    BigDecimal getBitcoinBalance(Address address, Integer minConf, Integer maxConf) {
        def btcBalance = new BigDecimal(0)
        def unspentOutputs = listUnspent(minConf, maxConf, [address])

        for (unspentOutput in unspentOutputs) {
            btcBalance += unspentOutput.amount
        }

        return btcBalance
    }

    /**
     * Sends BTC from an address to a destination, whereby no new change address is created, and any leftover is
     * returned to the sending address.
     *
     * @param fromAddress The source to spent from
     * @param toAddress   The destination address
     * @param amount      The amount to transfer
     * @return The transaction hash
     */
    Sha256Hash sendBitcoin(Address fromAddress, Address toAddress, BigDecimal amount) {
        def outputs = new HashMap<Address, BigDecimal>()
        outputs[toAddress] = amount
        return sendBitcoin(fromAddress, outputs)
    }

    /**
     * Sends BTC from an address to the destinations, whereby no new change address is created, and any leftover is
     * returned to the sending address.
     *
     * @param fromAddress The source to spent from
     * @param outputs     The destinations and amounts to transfer
     * @return The transaction hash
     */
    Sha256Hash sendBitcoin(Address fromAddress, Map<Address, BigDecimal> outputs) {
        def unsignedTxHex = createRawTransaction(fromAddress, outputs)
        def signingResult = signRawTransaction(unsignedTxHex)

        assert signingResult["complete"] == true

        def signedTxHex = signingResult["hex"] as String
        def txid = sendRawTransaction(signedTxHex)

        return txid
    }

    /**
     * Collects <b>all</b> unspent outputs and spends the whole amount minus {@code stdRelayTxFee}, which is sent
     * to a new address, as fee, to sweep dust and to minimize the number of unspent outputs, to avoid creating too
     * large transactions. No new block is generated afterwards.
     *
     * @see foundation.omni.BaseRegTestSpec#cleanup()
     * @see <a href="https://github.com/msgilligan/bitcoin-spock/issues/50">Issue #50 on GitHub</a>
     *
     * @return True, if enough outputs with a value of at least {@code stdRelayTxFee} were spent
     */
    Boolean consolidateCoins() {
        def amountIn = new BigDecimal(0)
        def inputs = new ArrayList<Outpoint>()
        def unspentOutputs = listUnspent(0, defaultMaxConf)

        // Gather inputs
        for (unspentOutput in unspentOutputs) {
            amountIn += unspentOutput.amount
            inputs << new Outpoint(unspentOutput.txid, unspentOutput.vout)
        }

        // Check if there is a sufficient high amount to sweep at all
        if (amountIn < stdRelayTxFee) {
            return false
        }

        // No receiver, just spend most of it as fee (!)
        def outputs = new HashMap<Address, BigDecimal>()
        outputs[newAddress] = stdRelayTxFee

        def unsignedTxHex = client.createRawTransaction(inputs, outputs)
        def signingResult = client.signRawTransaction(unsignedTxHex)

        assert signingResult.complete == true

        def signedTxHex = signingResult.hex as String
        def txid = client.sendRawTransaction(signedTxHex, true)

        return true
    }
}