package foundation.omni.test.rpc.sendall

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import org.junit.AssumptionViolatedException
import spock.lang.Unroll

class SendAllSpec extends BaseRegTestSpec {

    final static startBTC = 0.1.btc
    final static startMSC = 0.1.divisible
    final static zeroAmount = 0.0

    @Unroll
    def "In #ecosystem all available tokens can be transferred with transaction type 4"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def otherAddress = newAddress

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startMSC.numberValue()
        omniGetBalance(actorAddress, CurrencyID.TOMNI).balance == startMSC.numberValue()
        omniGetBalance(otherAddress, CurrencyID.OMNI).balance == zeroAmount
        omniGetBalance(otherAddress, CurrencyID.TOMNI).balance == zeroAmount

        when:
        def sendTxid = omniSendAll(actorAddress, otherAddress, ecosystem)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid"
        sendTx.valid

        and: "it has the specified values"
        sendTx.txid == sendTxid.toString()
        sendTx.sendingaddress == actorAddress.toString()
        sendTx.referenceaddress == otherAddress.toString()
        sendTx.type_int == 4
        sendTx.ecosystem == ecosystemToString(ecosystem)
        sendTx.containsKey('subsends')

        and:
        List<Map<String, Object>> subSends = sendTx['subsends']
        subSends.size() == 1
        subSends[0].propertyid == ecosystem.getValue()
        subSends[0].divisible
        subSends[0].amount as BigDecimal == startMSC.numberValue()

        and:
        if (ecosystem == Ecosystem.OMNI) {
            assert omniGetBalance(actorAddress, CurrencyID.OMNI).balance == zeroAmount
            assert omniGetBalance(actorAddress, CurrencyID.TOMNI).balance == startMSC.numberValue()
            assert omniGetBalance(otherAddress, CurrencyID.OMNI).balance == startMSC.numberValue()
            assert omniGetBalance(otherAddress, CurrencyID.TOMNI).balance == zeroAmount
        } else {
            assert omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startMSC.numberValue()
            assert omniGetBalance(actorAddress, CurrencyID.TOMNI).balance == zeroAmount
            assert omniGetBalance(otherAddress, CurrencyID.OMNI).balance == zeroAmount
            assert omniGetBalance(otherAddress, CurrencyID.TOMNI).balance == startMSC.numberValue()
        }

        where:
        ecosystem << [Ecosystem.OMNI, Ecosystem.TOMNI]
    }

    @Unroll
    def "In #ecosystem sending all tokens is only valid, if at least one unit was transferred"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def otherAddress = newAddress
        omniSend(actorAddress, otherAddress, CurrencyID.OMNI, startMSC)
        omniSend(actorAddress, otherAddress, CurrencyID.TOMNI, startMSC)
        generateBlocks(1)

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == zeroAmount
        omniGetBalance(actorAddress, CurrencyID.TOMNI).balance == zeroAmount
        omniGetBalance(otherAddress, CurrencyID.OMNI).balance == startMSC.numberValue()
        omniGetBalance(otherAddress, CurrencyID.TOMNI).balance == startMSC.numberValue()

        when:
        def sendTxid = omniSendAll(actorAddress, otherAddress, ecosystem)
        generateBlocks(1)

        then:
        omniGetTransaction(sendTxid).valid == false

        and:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == zeroAmount
        omniGetBalance(actorAddress, CurrencyID.TOMNI).balance == zeroAmount
        omniGetBalance(otherAddress, CurrencyID.OMNI).balance == startMSC.numberValue()
        omniGetBalance(otherAddress, CurrencyID.TOMNI).balance == startMSC.numberValue()

        where:
        ecosystem << [Ecosystem.OMNI, Ecosystem.TOMNI]
    }

    @Unroll
    def "In #ecosystem only available, unreserved balances are transferred, when sending all tokens"() {
        when:
        def actorAddress = createFundedAddress(startBTC, 0.divisible)
        def otherAddress = createFundedAddress(startBTC, 0.divisible)
        def nonManagedID = fundNewProperty(actorAddress, 10.divisible, ecosystem)
        def tradeCurrency = new CurrencyID(ecosystem.getValue())

        then:
        omniGetBalance(actorAddress, nonManagedID).balance == 10.0
        omniGetBalance(otherAddress, nonManagedID).balance == zeroAmount

        when:
        def tradeTxid = omniSendTrade(actorAddress, nonManagedID, 4.divisible, tradeCurrency, 4.divisible)
        generateBlocks(1)
        def tradeTx = omniGetTransaction(tradeTxid)

        then:
        tradeTx.valid
        omniGetBalance(actorAddress, nonManagedID).balance == 6.0
        omniGetBalance(actorAddress, nonManagedID).reserved == 4.0

        when:
        def sendTxid = omniSendAll(actorAddress, otherAddress, ecosystem)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then:
        sendTx.valid
        omniGetBalance(actorAddress, nonManagedID).balance == zeroAmount
        omniGetBalance(actorAddress, nonManagedID).reserved == 4.0
        omniGetBalance(otherAddress, nonManagedID).balance == 6.0

        where:
        ecosystem << [Ecosystem.OMNI, Ecosystem.TOMNI]
    }

    def ecosystemToString(Ecosystem ecosystem) {
        if (ecosystem == Ecosystem.OMNI) {
            return "main"
        } else {
            return "test"
        }
    }

    def setupSpec() {
        if (!commandExists("omni_sendall")) {
            throw new AssumptionViolatedException('The client has no "omni_sendall" command')
        }
    }

}
