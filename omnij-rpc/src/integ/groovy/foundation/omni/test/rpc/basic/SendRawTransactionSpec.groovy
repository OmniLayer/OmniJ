package foundation.omni.test.rpc.basic

import org.bitcoinj.core.Address
import foundation.omni.BaseRegTestSpec
import spock.lang.Shared

/**
 * Rudimentary tests of the raw transaction interface of Omni Core.
 */
class SendRawTransactionSpec extends BaseRegTestSpec {

    final static BigDecimal startBTC = 1.0
    final static BigDecimal startMSC = 50.0

    @Shared
    Address activeAddress
    Address passiveAddress

    def setup() {
        activeAddress = createFundedAddress(startBTC, startMSC)
        passiveAddress = getnewaddress()
    }

    def "Create raw transaction with reference address"() {
        when: "we submit a raw transaction"
        def txid = client.omniSendRawTx(activeAddress, rawTxHex, passiveAddress)

        and: "a new block is mined"
        client.generateBlock()

        then: "the transaction confirms"
        def transaction = client.omniGetTransaction(txid)
        transaction.confirmations == 1

        and: "the transaction should be valid"
        transaction.valid == true

        and: "we are the sender"
        transaction.sendingaddress == activeAddress.toString()

        and: "it has the reference address"
        transaction.referenceaddress == passiveAddress.toString()

        where:
        rawTxHex << ["00000000000000010000000000000001", // Simple Send: transfer  0.00000001 MSC
                     "000000000000000200000000cafebabe"] // Simple Send: transfer 34.05691582 TMSC
    }

    def "Create raw transaction without reference address"() {
        when: "we submit a raw transaction"
        def txid = client.omniSendRawTx(activeAddress, rawTxHex)

        and: "a new block is mined"
        client.generateBlock()

        then: "the transaction confirmed"
        def transaction = client.omniGetTransaction(txid)
        transaction.confirmations == 1

        and: "the transaction should be valid"
        transaction.valid == true

        and: "we are the sender"
        transaction.sendingaddress == activeAddress.toString()

        where:
        rawTxHex << [// Tx 20: Offer 1,0 MSC for 0.15 BTC on the distributed exchange
                     "00010014000000010000000005f5e1000000000000e4e1c00a000000000098968001",
                     // Tx 50: Create property in test ecosystem, 1000000 divisible "TDiv"
                     "000000320200020000000000005444697600000000000000000f4240",
                     // Tx 50: Create property in main ecosystem, 1123581321345589 indivisible "MIndiv"
                     "000000320100010000000000004d496e6469760000000003fde42988fa35"]
    }
}
