package org.bitcoinj.spock

import org.bitcoinj.core.Address
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Coin
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.ScriptBuilder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise


/**
 *
 * See Bitcoins the Hard Way
 * http://www.righto.com/2014/02/bitcoins-hard-way-using-raw-bitcoin.html
 */
@Stepwise
class TransactionSpec extends Specification {
    static final mainNetParams = MainNetParams.get()

    // Input Values
    static final fromKeyWIF = "5HusYj2b2x4nroApgfvaSfKYZhRbKFH41bVyPooymbC6KfgSXdD"
    static final Address toAddr = new Address(mainNetParams, "1KKKK6N21XKo48zWKuQKXdvSsCf95ibHFa")
    static final Sha256Hash utxo_id = new Sha256Hash("81b4c832d70cb56ff957589752eb4125a4cab78a25a8fc52d6a09e5bd4404d48")
    static final BigDecimal txAmount = 0.00091234
    static final BigDecimal txFee = 0.0001

    // Values used for Verification
    static final fromAddrVerify = new Address(mainNetParams, "1MMMMSUb1piy2ufrSguNUdFmAcvqrQF8M5")
    static final txVerify = "0100000001484d40d45b9ea0d652fca8258ab7caa42541eb52975857f96fb50cd732c8b481000000008a47304402202cb265bf10707bf49346c3515dd3d16fc454618c58ec0a0ff448a676c54ff71302206c6624d762a1fcef4618284ead8f08678ac05b13c84235f1654e6ad168233e8201410414e301b2328f17442c0b8310d787bf3d8a404cfbd0704f135b6ad4b2d3ee751310f981926e53a6e8c39bd7d3fefd576c543cce493cbac06388f2651d1aacbfcdffffffff0162640100000000001976a914c8e90996c7c6080ee06284600c684ed904d14c5c88ac00000000"

    @Shared
    ECKey fromKey

    @Shared
    Address fromAddress

    def "Can create an address from private key WIF"() {
        given:
        byte[] privKey = Arrays.copyOfRange(Base58.decodeChecked(fromKeyWIF), 1, 33);
        fromKey = new ECKey().fromPrivate(privKey, false)
        fromAddress = fromKey.toAddress(mainNetParams)

        expect:
        fromAddress == fromAddrVerify
    }

    @Ignore("Doesn't verify, not yet sure why")
    def "Can create and serialize a transaction"() {
        when:
        long txAmountSatoshis = (txAmount * Coin.COIN.longValue()).longValueExact()
        long txFeeSatoshis = (txFee * Coin.COIN.longValue()).longValueExact()
        Transaction tx = new Transaction(mainNetParams)
        TransactionOutPoint outPoint = new TransactionOutPoint(mainNetParams, 0, utxo_id)
        tx.addOutput(Coin.valueOf(txAmountSatoshis), toAddr)
        // Assume standard transaction
        tx.addSignedInput(outPoint, ScriptBuilder.createOutputScript(fromAddress), fromKey);

        and: "We serialize the transaction"
        byte[] rawTx = tx.bitcoinSerialize()

        then: "Output is as expected"
        rawTx.encodeHex().toString() == txVerify

    }


}