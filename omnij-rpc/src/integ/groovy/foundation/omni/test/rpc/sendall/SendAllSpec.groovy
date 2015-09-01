package foundation.omni.test.rpc.sendall

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import org.junit.internal.AssumptionViolatedException
import spock.lang.Unroll

class SendAllSpec extends BaseRegTestSpec {

    final static BigDecimal startBTC = 0.1
    final static BigDecimal startMSC = 0.1
    final static BigDecimal zeroAmount = 0.0

    @Unroll
    def "In #ecosystem all available tokens can be transferred with transaction type 4"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def otherAddress = newAddress

        then:
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == startMSC
        omniGetBalance(actorAddress, CurrencyID.TMSC).balance == startMSC
        omniGetBalance(otherAddress, CurrencyID.MSC).balance == zeroAmount
        omniGetBalance(otherAddress, CurrencyID.TMSC).balance == zeroAmount

        when:
        def sendTxid = omniSendAll(actorAddress, otherAddress, ecosystem)
        generateBlock()
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
        subSends[0].amount as BigDecimal == startMSC

        and:
        if (ecosystem == Ecosystem.MSC) {
            assert omniGetBalance(actorAddress, CurrencyID.MSC).balance == zeroAmount
            assert omniGetBalance(actorAddress, CurrencyID.TMSC).balance == startMSC
            assert omniGetBalance(otherAddress, CurrencyID.MSC).balance == startMSC
            assert omniGetBalance(otherAddress, CurrencyID.TMSC).balance == zeroAmount
        } else {
            assert omniGetBalance(actorAddress, CurrencyID.MSC).balance == startMSC
            assert omniGetBalance(actorAddress, CurrencyID.TMSC).balance == zeroAmount
            assert omniGetBalance(otherAddress, CurrencyID.MSC).balance == zeroAmount
            assert omniGetBalance(otherAddress, CurrencyID.TMSC).balance == startMSC
        }

        where:
        ecosystem << [Ecosystem.MSC, Ecosystem.TMSC]
    }

    @Unroll
    def "In #ecosystem sending all tokens is only valid, if at least one unit was transferred"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def otherAddress = newAddress
        omniSend(actorAddress, otherAddress, CurrencyID.MSC, startMSC)
        omniSend(actorAddress, otherAddress, CurrencyID.TMSC, startMSC)
        generateBlock()

        then:
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == zeroAmount
        omniGetBalance(actorAddress, CurrencyID.TMSC).balance == zeroAmount
        omniGetBalance(otherAddress, CurrencyID.MSC).balance == startMSC
        omniGetBalance(otherAddress, CurrencyID.TMSC).balance == startMSC

        when:
        def sendTxid = omniSendAll(actorAddress, otherAddress, ecosystem)
        generateBlock()

        then:
        omniGetTransaction(sendTxid).valid == false

        and:
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == zeroAmount
        omniGetBalance(actorAddress, CurrencyID.TMSC).balance == zeroAmount
        omniGetBalance(otherAddress, CurrencyID.MSC).balance == startMSC
        omniGetBalance(otherAddress, CurrencyID.TMSC).balance == startMSC

        where:
        ecosystem << [Ecosystem.MSC, Ecosystem.TMSC]
    }

    @Unroll
    def "In #ecosystem only available, unreserved balances are transferred, when sending all tokens"() {
        when:
        def actorAddress = createFundedAddress(startBTC, zeroAmount)
        def otherAddress = createFundedAddress(startBTC, zeroAmount)
        def nonManagedID = fundNewProperty(actorAddress, 10.0, PropertyType.DIVISIBLE, ecosystem)
        def tradeCurrency = new CurrencyID(ecosystem.getValue())

        then:
        omniGetBalance(actorAddress, nonManagedID).balance == 10.0
        omniGetBalance(otherAddress, nonManagedID).balance == zeroAmount

        when:
        def tradeTxid = omniSendTrade(actorAddress, nonManagedID, 4.0, tradeCurrency, 4.0)
        generateBlock()
        def tradeTx = omniGetTransaction(tradeTxid)

        then:
        tradeTx.valid
        omniGetBalance(actorAddress, nonManagedID).balance == 6.0
        omniGetBalance(actorAddress, nonManagedID).reserved == 4.0

        when:
        def sendTxid = omniSendAll(actorAddress, otherAddress, ecosystem)
        generateBlock()
        def sendTx = omniGetTransaction(sendTxid)

        then:
        sendTx.valid
        omniGetBalance(actorAddress, nonManagedID).balance == zeroAmount
        omniGetBalance(actorAddress, nonManagedID).reserved == 4.0
        omniGetBalance(otherAddress, nonManagedID).balance == 6.0

        where:
        ecosystem << [Ecosystem.MSC, Ecosystem.TMSC]
    }

    def ecosystemToString(Ecosystem ecosystem) {
        if (ecosystem == Ecosystem.MSC) {
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
