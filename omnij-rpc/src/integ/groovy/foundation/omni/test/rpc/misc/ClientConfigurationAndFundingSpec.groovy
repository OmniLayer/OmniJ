package foundation.omni.test.rpc.misc
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import org.bitcoinj.core.Address
import spock.lang.Ignore
import spock.lang.Unroll

class ClientConfigurationAndFundingSpec extends BaseRegTestSpec {

    def "A newly generated address starts with 0.0 BTC, 0.0 MSC and 0.0 TMSC"() {
        given:
        def pristineAddress = newAddress

        def balanceBTC = getBitcoinBalance(pristineAddress, 0, 9999999)
        def balanceMSC = omniGetBalance(pristineAddress, CurrencyID.MSC)
        def balanceTMSC = omniGetBalance(pristineAddress, CurrencyID.TMSC)

        expect: "zero balances"
        balanceBTC == 0.0
        balanceMSC.balance == 0.0
        balanceMSC.reserved == 0.0
        balanceTMSC.balance == 0.0
        balanceTMSC.reserved == 0.0
    }

    @Unroll
    def "Requesting #requestedBTC BTC adds exactly that amount to the receivers BTC balance"() {
        given:
        def fundedAddress = newAddress
        def balanceAtStart = getBitcoinBalance(fundedAddress, 0)

        when: "requesting bitcoin"
        def txid = requestBitcoin(fundedAddress, requestedBTC)

        then: "the requested amount was credited"
        def balanceAfterRequest = getBitcoinBalance(fundedAddress, 0)
        balanceAfterRequest == balanceAtStart + requestedBTC

        when:
        generateBlock()

        then:
        def fundingTx = getTransaction(txid)
        def finalBalance = getBitcoinBalance(fundedAddress, 1)
        fundingTx.confirmations > 0
        finalBalance == balanceAtStart + requestedBTC
        finalBalance == balanceAfterRequest

        where:
        requestedBTC << [1.0, stdTxFee, stdRelayTxFee, 0.00000001]
    }

    @Unroll
    def "The client accepts a transaction fee of #feeBTC BTC"() {
        def sendBTC = 0.1
        def startBTC = sendBTC + feeBTC

        def senderAddress = newAddress
        def receiverAddress = newAddress
        requestBitcoin(senderAddress, startBTC)
        generateBlock()

        when: "sending a transaction which consumes the whole balance except fee"
        def outputs = new HashMap<Address, BigDecimal>()
        outputs[receiverAddress] = sendBTC
        def txid = sendBitcoin(senderAddress, outputs)
        generateBlock()

        then: "sender ends up with zero and receiver with the transferred amount, implying the rest was spent for fees"
        def sendTx = getTransaction(txid)
        sendTx.confirmations == 1
        getBitcoinBalance(senderAddress) == 0.0
        getBitcoinBalance(receiverAddress) == sendBTC

        where:
        feeBTC = stdTxFee
    }

    def "Calculate dust amounts for common transaction output types"() {
        given:
        def relayTxFee = new BigDecimal('0.00001000')

        expect: "correct results from Groovy for calculating dust amounts"
        // Pay-to-pubkey-hash (34 byte)
        ((((148 + 34) * 3) / 1000) * relayTxFee).setScale(8, BigDecimal.ROUND_UP) == new BigDecimal('0.00000546')

        // Multisig, two compressed public keys (80 byte)
        ((((148 + 80) * 3) / 1000) * relayTxFee).setScale(8, BigDecimal.ROUND_UP) == new BigDecimal('0.00000684')

        // Multisig, three compressed public keys (114 byte)
        ((((148 + 114) * 3) / 1000) * relayTxFee).setScale(8, BigDecimal.ROUND_UP) == new BigDecimal('0.00000786')

        // Multisig, one uncompressed, two compressed public keys (146 byte)
        ((((148 + 146) * 3) / 1000) * relayTxFee).setScale(8, BigDecimal.ROUND_UP) == new BigDecimal('0.00000882')
    }

    @Unroll
    def "The client accepts dust outputs with #dustAmount BTC"() {
        def startBTC = dustAmount + stdTxFee

        def senderAddress = newAddress
        def receiverAddress = newAddress
        requestBitcoin(senderAddress, startBTC)
        generateBlock()

        def outputs = new HashMap<Address, BigDecimal>()
        outputs[receiverAddress] = dustAmount

        when: "sending a transaction with a dust output"
        def txid = sendBitcoin(senderAddress, outputs)
        generateBlock()

        then: "it is valid and not rejected"
        def sendTx = getTransaction(txid)
        sendTx.confirmations == 1
        getBitcoinBalance(senderAddress) == 0.0
        getBitcoinBalance(receiverAddress) == dustAmount

        where:
        relayTxFee = stdRelayTxFee
        dustAmount = ((((148 + 34) * 3) / 1000) * relayTxFee).setScale(8, BigDecimal.ROUND_UP)
    }

    @Ignore
    @Unroll
    def "The client generates a \"simple send\" transaction with 2x #payToPubKeyDust + 1x #payloadDust BTC outputs"() {
        def startMSC = 1.0
        def startBTC = 2 * payToPubKeyDust + payloadDust + stdTxFee

        def senderAddress = newAddress
        def receiverAddress = newAddress

        when:
        requestBitcoin(senderAddress, startBTC)
        requestMSC(senderAddress, startMSC)
        generateBlock()

        then:
        getBitcoinBalance(receiverAddress) == 0.0
        getBitcoinBalance(senderAddress) == startBTC
        omniGetBalance(receiverAddress, CurrencyID.MSC).balance == 0.0
        omniGetBalance(senderAddress, CurrencyID.MSC).balance == startMSC

        when:
        def txid = omniSend(senderAddress, receiverAddress, CurrencyID.MSC, startMSC)
        generateBlock()

        then:
        getBitcoinBalance(receiverAddress) == payToPubKeyDust
        getBitcoinBalance(senderAddress) == 0.0

        and:
        def sendTx = omniGetTransaction(txid)
        sendTx.confirmations == 1
        sendTx.valid == true
        omniGetBalance(receiverAddress, CurrencyID.MSC).balance == startMSC
        omniGetBalance(senderAddress, CurrencyID.MSC).balance == 0.0

        where:
        relayTxFee = stdRelayTxFee
        payToPubKeyDust = ((((148 + 34) * 3) / 1000) * stdRelayTxFee).setScale(8, BigDecimal.ROUND_UP)
        payloadDust = ((((148 + 80) * 3) / 1000) * stdRelayTxFee).setScale(8, BigDecimal.ROUND_UP)
    }

    @Unroll
    def "Requesting #requestedMSC MSC adds exactly that amount to the receivers MSC balance"() {
        given:
        def fundedAddress = newAddress
        def balanceAtStart = omniGetBalance(fundedAddress, CurrencyID.MSC)

        when:
        def txid = requestMSC(fundedAddress, requestedMSC)
        generateBlock()

        then:
        def fundingTx = getTransaction(txid)
        def balanceConfirmed = omniGetBalance(fundedAddress, CurrencyID.MSC)

        fundingTx.confirmations > 0
        balanceConfirmed.balance == balanceAtStart.balance + requestedMSC
        balanceConfirmed.reserved == balanceAtStart.reserved

        where:
        requestedMSC << [1.0, 0.1, 0.000001, 0.00000001, 0.00000001]
    }

}
