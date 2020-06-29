package foundation.omni.test.rpc.misc
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.OmniDivisibleValue
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import spock.lang.Ignore
import spock.lang.Unroll

class ClientConfigurationAndFundingSpec extends BaseRegTestSpec {

    def "A newly generated address starts with 0.0 BTC, 0.0 MSC and 0.0 TMSC"() {
        given:
        def pristineAddress = newAddress

        Coin balanceBTC = getBitcoinBalance(pristineAddress, 0, 9999999)
        def balanceMSC = omniGetBalance(pristineAddress, CurrencyID.OMNI)
        def balanceTMSC = omniGetBalance(pristineAddress, CurrencyID.TOMNI)

        expect: "zero balances"
        balanceBTC == Coin.ZERO
        balanceMSC.balance == 0.0.divisible
        balanceMSC.reserved.numberValue() == 0.0.divisible
        balanceTMSC.balance.numberValue() == 0.0.divisible
        balanceTMSC.reserved.numberValue() == 0.0.divisible
    }

    @Unroll
    def "Requesting #requestedBTC BTC adds exactly that amount to the receivers BTC balance"(Coin requestedBTC) {
        given:
        def fundedAddress = newAddress
        Coin balanceAtStart = getBitcoinBalance(fundedAddress, 0)

        when: "requesting bitcoin"
        def txid = requestBitcoin(fundedAddress, requestedBTC)

        then: "the requested amount was credited"
        def balanceAfterRequest = getBitcoinBalance(fundedAddress, 0)
        balanceAfterRequest == balanceAtStart + requestedBTC

        when:
        generateBlocks(1)

        then:
        def fundingTx = getTransaction(txid)
        def finalBalance = getBitcoinBalance(fundedAddress, 1)
        fundingTx.confirmations > 0
        finalBalance == balanceAtStart + requestedBTC
        finalBalance == balanceAfterRequest

        where:
        requestedBTC << [1.btc, stdTxFee, stdRelayTxFee, 0.00000001.btc]
    }

    @Unroll
    def "The client accepts a transaction fee of #feeBTC BTC"() {
        def sendBTC = 0.1.btc
        def startBTC = sendBTC + feeBTC

        def senderAddress = newAddress
        def receiverAddress = newAddress
        requestBitcoin(senderAddress, startBTC)
        generateBlocks(1)

        when: "sending a transaction which consumes the whole balance except fee"
        def outputs = new HashMap<Address, Coin>()
        outputs[receiverAddress] = sendBTC
        def txid = sendBitcoin(senderAddress, outputs)
        generateBlocks(1)

        then: "sender ends up with zero and receiver with the transferred amount, implying the rest was spent for fees"
        def sendTx = getTransaction(txid)
        sendTx.confirmations == 1
        getBitcoinBalance(senderAddress) == 0.btc
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
    def "The client accepts dust outputs with #dustAmount BTC"(Coin relayTxFee, BigInteger dustAmount) {
        def startBTC = dustAmount.satoshi + stdTxFee

        def senderAddress = newAddress
        def receiverAddress = newAddress
        requestBitcoin(senderAddress, startBTC)
        generateBlocks(1)

        def outputs = new HashMap<Address, Coin>()
        outputs[receiverAddress] = dustAmount.satoshi

        when: "sending a transaction with a dust output"
        def txid = sendBitcoin(senderAddress, outputs)
        generateBlocks(1)

        then: "it is valid and not rejected"
        def sendTx = getTransaction(txid)
        sendTx.confirmations == 1
        getBitcoinBalance(senderAddress) == 0.btc
        getBitcoinBalance(receiverAddress) == dustAmount.satoshi

        where:
        relayTxFee = stdRelayTxFee
        dustAmount = ((((148 + 34) * 3) / 1000) * relayTxFee.value).setScale(8, BigDecimal.ROUND_UP)
    }

    @Ignore
    @Unroll
    def "The client generates a \"simple send\" transaction with 2x #payToPubKeyDust + 1x #payloadDust BTC outputs"() {
        def startMSC = 1.0.divisible
        def startBTC = (2 * payToPubKeyDust + payloadDust + stdTxFee.value).satoshi

        def senderAddress = newAddress
        def receiverAddress = newAddress

        when:
        requestBitcoin(senderAddress, startBTC)
        requestMSC(senderAddress, startMSC)
        generateBlocks(1)

        then:
        getBitcoinBalance(receiverAddress) == 0.btc
        getBitcoinBalance(senderAddress) == startBTC
        omniGetBalance(receiverAddress, CurrencyID.OMNI).balance == 0.0.divisible
        omniGetBalance(senderAddress, CurrencyID.OMNI).balance == startMSC

        when:
        def txid = omniSend(senderAddress, receiverAddress, CurrencyID.OMNI, startMSC)
        generateBlocks(1)

        then:
        getBitcoinBalance(receiverAddress) == payToPubKeyDust.satoshi
        getBitcoinBalance(senderAddress) == 0.btc

        and:
        def sendTx = omniGetTransaction(txid)
        sendTx.confirmations == 1
        sendTx.valid == true
        omniGetBalance(receiverAddress, CurrencyID.OMNI).balance == startMSC
        omniGetBalance(senderAddress, CurrencyID.OMNI).balance == 0.0.divisible

        where:
        relayTxFee = stdRelayTxFee
        payToPubKeyDust = ((((148 + 34) * 3) / 1000) * stdRelayTxFee.value).setScale(8, BigDecimal.ROUND_UP)
        payloadDust = ((((148 + 80) * 3) / 1000) * stdRelayTxFee.value).setScale(8, BigDecimal.ROUND_UP)
    }

    @Unroll
    def "Requesting #requestedAmount MSC adds exactly that amount to the receivers MSC balance"() {
        given:
        def fundedAddress = newAddress
        def balanceAtStart = omniGetBalance(fundedAddress, CurrencyID.OMNI)

        when:
        def requestedOmni = OmniDivisibleValue.of(requestedAmount)
        def txid = requestMSC(fundedAddress, requestedOmni)
        generateBlocks(1)

        then:
        def fundingTx = getTransaction(txid)
        def balanceConfirmed = omniGetBalance(fundedAddress, CurrencyID.OMNI)

        fundingTx.confirmations > 0
        balanceConfirmed.balance == balanceAtStart.balance + requestedOmni.numberValue()
        balanceConfirmed.reserved == balanceAtStart.reserved

        where:
        requestedAmount << [1.0, 0.1, 0.000001, 0.00000001, 0.00000001]
    }

}
