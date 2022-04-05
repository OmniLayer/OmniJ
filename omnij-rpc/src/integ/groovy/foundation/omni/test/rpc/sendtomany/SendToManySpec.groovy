package foundation.omni.test.rpc.sendtomany

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.OmniOutput
import foundation.omni.OmniValue
import org.junit.AssumptionViolatedException

class SendToManySpec extends BaseRegTestSpec {

    final static startBTC = 0.1.btc
    final static startOmni = 1.0.divisible

    def "Send to many with seven receivers with Class C encoding"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress
        def otherAddress3 = newAddress
        def otherAddress4 = newAddress
        def otherAddress5 = newAddress
        def otherAddress6 = newAddress
        def otherAddress7 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress2, 0.00000002.divisible),
                new OmniOutput(otherAddress3, 0.00000003.divisible),
                new OmniOutput(otherAddress4, 0.00000004.divisible),
                new OmniOutput(otherAddress5, 0.00000005.divisible),
                new OmniOutput(otherAddress6, 0.00000006.divisible),
                new OmniOutput(otherAddress7, 0.00000007.divisible)
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending to seven receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid and all destinations receivers the correct balance"
        sendTx.valid
        OmniValue.of(sendTx.totalamount) == 0.00000028.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000001.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000002.divisible
        omniGetBalance(otherAddress3, CurrencyID.OMNI).balance == 0.00000003.divisible
        omniGetBalance(otherAddress4, CurrencyID.OMNI).balance == 0.00000004.divisible
        omniGetBalance(otherAddress5, CurrencyID.OMNI).balance == 0.00000005.divisible
        omniGetBalance(otherAddress6, CurrencyID.OMNI).balance == 0.00000006.divisible
        omniGetBalance(otherAddress7, CurrencyID.OMNI).balance == 0.00000007.divisible
    }

    def "Send to many with eight receivers with Class B encoding"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress
        def otherAddress3 = newAddress
        def otherAddress4 = newAddress
        def otherAddress5 = newAddress
        def otherAddress6 = newAddress
        def otherAddress7 = newAddress
        def otherAddress8 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress2, 0.00000002.divisible),
                new OmniOutput(otherAddress3, 0.00000003.divisible),
                new OmniOutput(otherAddress4, 0.00000004.divisible),
                new OmniOutput(otherAddress5, 0.00000005.divisible),
                new OmniOutput(otherAddress6, 0.00000006.divisible),
                new OmniOutput(otherAddress7, 0.00000007.divisible),
                new OmniOutput(otherAddress8, 0.00000008.divisible)
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending to eight receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid and all destinations receivers the correct balance"
        sendTx.valid
        OmniValue.of(sendTx.totalamount) == 0.00000036.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000001.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000002.divisible
        omniGetBalance(otherAddress3, CurrencyID.OMNI).balance == 0.00000003.divisible
        omniGetBalance(otherAddress4, CurrencyID.OMNI).balance == 0.00000004.divisible
        omniGetBalance(otherAddress5, CurrencyID.OMNI).balance == 0.00000005.divisible
        omniGetBalance(otherAddress6, CurrencyID.OMNI).balance == 0.00000006.divisible
        omniGetBalance(otherAddress7, CurrencyID.OMNI).balance == 0.00000007.divisible
        omniGetBalance(otherAddress8, CurrencyID.OMNI).balance == 0.00000008.divisible
    }

    def "Send to many with 221 receivers with Class B encoding"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddressA = newAddress
        def otherAddressB = newAddress

        ArrayList<OmniOutput> mapping = []
        for (def i = 0; i < 220; i++) {
            mapping.add(new OmniOutput(otherAddressA, 0.00000001.divisible))
        }
        mapping.add(new OmniOutput(otherAddressB, 0.00000007.divisible))

        then:
        omniGetBalance(actorAddress, CurrencyID.TOMNI).balance == startOmni.numberValue()

        when: "sending to 220 receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.TOMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid and the receiver received the correct number of tokens"
        sendTx.valid
        OmniValue.of(sendTx.totalamount) == 0.00000227.divisible
        omniGetBalance(otherAddressA, CurrencyID.TOMNI).balance == 0.00000220.divisible
        omniGetBalance(otherAddressB, CurrencyID.TOMNI).balance == 0.00000007.divisible
    }

    def setupSpec() {
        if (!commandExists("omni_sendtomany")) {
            throw new AssumptionViolatedException('The client has no "omni_sendtomany" command')
        }
    }

}
