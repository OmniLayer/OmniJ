package foundation.omni.test

import org.bitcoinj.core.Address
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import com.msgilligan.bitcoin.BTC
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.MPNetworkParameters
import foundation.omni.MPRegTestParams
import foundation.omni.PropertyType
import foundation.omni.rpc.MastercoinClientDelegate

import java.security.SecureRandom

/**
 * Test support functions intended to be mixed-in to Spock test specs
 */
trait TestSupport implements MastercoinClientDelegate {
    // TODO: For some reason when we upgraded to bitcoinj 12.2 (from 11.3) some integration tests
    // broke because they were assuming the old transaction fee (or is this because Omni Core itself does)
    // so, temporarily this constant is using 10x what bitcoinj is calling the default min tx fee.
    final BigDecimal stdTxFee = BTC.satoshisToBTC(10 * Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.value)

    String createNewAccount() {
        def random = new SecureRandom();
        def accountName = "msc-" + new BigInteger(130, random).toString(32)
        return accountName
    }

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
            if (txout.containsKey("value")) {
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
        final MPNetworkParameters params = MPRegTestParams.get()  // Hardcoded for RegTest for now

        // TODO: avoid inaccurate funding
        // TODO: integrate into createFundedAddress
        def btcForMSC = (requestedMSC / 100).setScale(8, BigDecimal.ROUND_UP)
        requestBitcoin(toAddress, btcForMSC + stdTxFee)

        def txid = sendBitcoin(toAddress, params.moneyManAddress, btcForMSC)
        return txid
    }

    Address createFundedAddress(BigDecimal requestedBTC, BigDecimal requestedMSC) {
        final MPNetworkParameters params = MPRegTestParams.get()  // Hardcoded for RegTest for now
        Address stepAddress = getNewAddress()
        def btcForMSC = (requestedMSC / 100).setScale(8, BigDecimal.ROUND_UP)
        def startBTC = requestedBTC + btcForMSC + stdTxFee
        startBTC += 5 * stdTxFee  // To send BTC, MSC and TMSC to the real receiver

        Sha256Hash txid = requestBitcoin(stepAddress, startBTC)
        generateBlock()
        def tx = getTransaction(txid)
        assert tx.confirmations == 1

        // Make sure we got the correct amount of BTC
        BigDecimal btcBalance = getBitcoinBalance(stepAddress)
        assert btcBalance == startBTC

        // Send BTC to get MSC (and TMSC)
        txid = sendBitcoin(stepAddress, params.moneyManAddress, btcForMSC)
        generateBlock()
        tx = getTransaction(txid)
        assert tx.confirmations == 1

        // Send to the actual destination
        Address fundedAddress = getNewAddress()
        send_MP(stepAddress, fundedAddress, CurrencyID.MSC, requestedMSC)
        send_MP(stepAddress, fundedAddress, CurrencyID.TMSC, requestedMSC)
        generateBlock()
        def remainingBTC = requestedBTC - getBitcoinBalance(fundedAddress)
        txid = sendBitcoin(stepAddress, fundedAddress, remainingBTC)
        generateBlock()
        tx = getTransaction(txid)
        assert tx.confirmations == 1

        // Verify correct amounts received
        btcBalance = getBitcoinBalance(fundedAddress)
        BigDecimal mscBalance = getbalance_MP(fundedAddress, CurrencyID.MSC).balance
        BigDecimal tmscBalance = getbalance_MP(fundedAddress, CurrencyID.TMSC).balance

        assert btcBalance == requestedBTC
        assert mscBalance == requestedMSC
        assert tmscBalance == requestedMSC

        return fundedAddress
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
        def unspentOutputs = listUnspent(0, 999999, [fromAddress])

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
        return getBitcoinBalance(address, 1, 99999)
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
     * Creates and broadcasts a "send to owners" transaction.
     *
     * @param currencyId  The identifier of the currency
     * @param amount      The number of tokens to distribute
     * @return The transaction hash
     */
    Sha256Hash sendToOwners(Address address, CurrencyID currencyId, Long amount) {
        def rawTxHex = createSendToOwnersHex(currencyId, amount);
        def txid = sendrawtx_MP(address, rawTxHex)
        return txid
    }

    /**
     * Creates an offer on the traditional distributed exchange.
     *
     * @param address        The address
     * @param currencyId     The identifier of the currency for sale
     * @param amountForSale  The amount of currency
     * @param amountDesired  The amount of desired Bitcoin
     * @param paymentWindow  The payment window measured in blocks
     * @param commitmentFee  The minimum transaction fee required to be paid as commmitment when accepting this offer
     * @param action         The action applied to the offer (1 = new, 2 = update, 3 = cancel)
     * @return The transaction hash
     */
    Sha256Hash createDexSellOffer(Address address, CurrencyID currencyId, BigDecimal amountForSale,
                                  BigDecimal amountDesired, Number paymentWindow, BigDecimal commitmentFee,
                                  Number action) {
        def rawTxHex = createDexSellOfferHex(
                currencyId, amountForSale, amountDesired, paymentWindow, commitmentFee, action);
        def txid = sendrawtx_MP(address, rawTxHex)
        return txid
    }

    /**
     * Creates a smart property with fixed supply.
     *
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param type       The property type
     * @param amount     The number of units to create
     * @return The transaction hash
     */
    Sha256Hash createProperty(Address address, Ecosystem ecosystem, PropertyType type, Long amount) {
        return createProperty(address, ecosystem, type, amount, "SP");
    }

    /**
     * Creates a smart property with fixed supply.
     *
     * @param address    The issuance address
     * @param ecosystem  The ecosystem to create the property in
     * @param type       The property type
     * @param amount     The number of units to create
     * @param label      The label or title of the property
     * @return The transaction hash
     */
    Sha256Hash createProperty(Address address, Ecosystem ecosystem, PropertyType type, Long amount, String label) {
        def rawTxHex = createPropertyHex(ecosystem, type, 0L, "", "", label, "", "", amount);
        def txid = sendrawtx_MP(address, rawTxHex)
        return txid
    }

    /**
     * Creates a hex-encoded raw transaction of type 3: "send to owners".
     */
    String createSendToOwnersHex(CurrencyID currencyId, Long amount) {
        def rawTxHex = String.format("00000003%08x%016x", currencyId.longValue(), amount)
        return rawTxHex
    }

    /**
     * Creates a hex-encoded raw transaction of type 20: "sell mastercoin for bitcoin".
     */
    String createDexSellOfferHex(CurrencyID currencyId, BigDecimal amountForSale, BigDecimal amountDesired,
                                 Number paymentWindow, BigDecimal commitmentFee, Number action) {
        def rawTxHex = String.format("00010014%08x%016x%016x%02x%016x%02x",
                currencyId.longValue(),
                (BTC.btcToSatoshis(amountForSale)).longValue(),
                (BTC.btcToSatoshis(amountDesired)).longValue(),
                paymentWindow.byteValue(),
                (BTC.btcToSatoshis(commitmentFee)).longValue(),
                action.byteValue())
        return rawTxHex
    }

    /**
     * Creates a hex-encoded raw transaction of type 50: "create property with fixed supply".
     */
    String createPropertyHex(Ecosystem ecosystem, PropertyType propertyType, Long previousPropertyId,
                             String category, String subCategory, String label, String website, String info,
                             Long amount) {
        def rawTxHex = String.format("00000032%02x%04x%08x%s00%s00%s00%s00%s00%016x",
                                     ecosystem.byteValue(),
                                     propertyType.intValue(),
                                     previousPropertyId,
                                     toHexString(category),
                                     toHexString(subCategory),
                                     toHexString(label),
                                     toHexString(website),
                                     toHexString(info),
                                     amount)
        return rawTxHex
    }

    /**
     * Converts an UTF-8 encoded String into a hexadecimal string representation.
     *
     * @param str The string
     * @return The hexadecimal representation
     */
    String toHexString(String str) {
        def ba = str.getBytes("UTF-8")
        return toHexString(ba)
    }

    /**
     * Converts a byte array into a hexadecimal string representation.
     *
     * @param ba The byte array
     * @return The hexadecimal representation
     */
    String toHexString(byte[] ba) {
        StringBuilder str = new StringBuilder()
        for (int i = 0; i < ba.length; i++) {
            str.append(String.format("%x", ba[i]))
        }
        return str.toString()
    }

}
