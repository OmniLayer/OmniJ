package foundation.omni.test

import foundation.omni.rpc.ExtendedTransactions
import org.bitcoinj.core.Address
import org.bitcoinj.core.Sha256Hash
import foundation.omni.CurrencyID
import foundation.omni.net.OmniNetworkParameters
import foundation.omni.net.OmniRegTestParams
import foundation.omni.rpc.OmniClientDelegate

import java.security.SecureRandom

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait TestSupport implements OmniClientDelegate, ExtendedTransactions {
    // TODO: set, or get and verify default values of the client
    final BigDecimal stdTxFee = new BigDecimal('0.00010000')
    final BigDecimal stdRelayTxFee = new BigDecimal('0.00001000')
    final Integer defaultMaxConf = 9999999

    String createNewAccount() {
        def random = new SecureRandom();
        def accountName = "msc-" + new BigInteger(130, random).toString(32)
        return accountName
    }

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
        def inputs = new ArrayList<Map<String, Object>>()

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
                def coinbaseTxid = coinbaseTx.toString()
                inputs << ["txid": coinbaseTxid, "vout": 0]
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

    Sha256Hash requestMSC(Address toAddress, BigDecimal requestedMSC) {
        return requestMSC(toAddress, requestedMSC, true)
    }

    Sha256Hash requestMSC(Address toAddress, BigDecimal requestedMSC, Boolean allowIntermediate) {
        final OmniNetworkParameters params = OmniRegTestParams.get()  // Hardcoded for RegTest for now

        // For 1.0 BTC an amount of 100.0 MSC is generated, resulting in a minimal purchase amount of
        // 0.00000100 MSC for 0.00000001 BTC
        def btcForMSC = (requestedMSC / 100.0).setScale(8, BigDecimal.ROUND_UP)
        def actualMSC = btcForMSC * 100.0

        if (!allowIntermediate) {
            assert actualMSC == requestedMSC
        }

        requestBitcoin(toAddress, btcForMSC + stdTxFee)
        def txid = sendBitcoin(toAddress, params.moneyManAddress, btcForMSC)

        if (actualMSC != requestedMSC) {
            def excessiveMSC = actualMSC - requestedMSC

            // TODO: avoid magic numbers for dust calculation
            def dustForExodus = ((((148 + 34) * 3) / 1000) * stdRelayTxFee).setScale(8, BigDecimal.ROUND_UP)
            def dustForReference = ((((148 + 34) * 3) / 1000) * stdRelayTxFee).setScale(8, BigDecimal.ROUND_UP)
            def dustForPayload = ((((148 + 80) * 3) / 1000) * stdRelayTxFee).setScale(8, BigDecimal.ROUND_UP)

            // Simple send transactions have a dust output for the receiver reference, a marker output and an output
            // for the actual payload. MSC and TMSC are forwarded in two transactions, so this amount, as well as the
            // transaction fee, have to be paid twice
            def additionalRequiredBTC = 2 * (dustForExodus + dustForReference + dustForPayload + stdTxFee)
            requestBitcoin(toAddress, additionalRequiredBTC)

            // The excessive amount of MSC is sent to a new address to get rid of it
            def junkAddress = newAddress

            // TODO: can we always get away with not generating a block inbetween?
            def extraTxidMSC = send_MP(toAddress, junkAddress, CurrencyID.MSC, excessiveMSC)
            def extraTxidTMSC = send_MP(toAddress, junkAddress, CurrencyID.TMSC, excessiveMSC)
        }

        // TODO: when using an intermediate receiver, this txid doesn't reflect the whole picture
        return txid
    }

    Address createFundedAddress(BigDecimal requestedBTC, BigDecimal requestedMSC) {
        return createFundedAddress(requestedBTC, requestedMSC, true)
    }

    Address createFundedAddress(BigDecimal requestedBTC, BigDecimal requestedMSC, Boolean confirmTransactions) {
        def fundedAddress = newAddress

        if (requestedMSC > 0.0) {
            def txidMSC = requestMSC(fundedAddress, requestedMSC)
        }

        if (requestedBTC > 0.0) {
            def txidBTC = requestBitcoin(fundedAddress, requestedBTC)
        }

        if (confirmTransactions) {
            generateBlock()
        }

        // TODO: maybe add assertions to check correct funding amounts?

        return fundedAddress
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
        def inputs = new ArrayList<Map<String, Object>>()
        def unspentOutputs = listUnspent(0, defaultMaxConf, [fromAddress])

        // Gather inputs
        for (unspentOutput in unspentOutputs) {
            def outpoint = new HashMap<String, Object>()
            def amountBTCd = unspentOutput["amount"] as Double // TODO: Don't use Double
            amountIn += BigDecimal.valueOf(amountBTCd)
            outpoint["txid"] = unspentOutput["txid"]
            outpoint["vout"] = unspentOutput["vout"]
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
        def unspentOutputs = (List<Map<String, Object>>) listUnspent(minConf, maxConf, [address])

        for (unspentOutput in unspentOutputs) {
            def balanceBTCd = unspentOutput["amount"] as Double
            btcBalance += BigDecimal.valueOf(balanceBTCd)
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
        def inputs = new ArrayList<Map<String, Object>>()
        def unspentOutputs = listUnspent(0, defaultMaxConf)

        // Gather inputs
        for (unspentOutput in unspentOutputs) {
            def amountBTCd = unspentOutput['amount'] as Double
            amountIn += BigDecimal.valueOf(amountBTCd)
            inputs << ['txid': unspentOutput['txid'], 'vout': unspentOutput['vout']]
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
