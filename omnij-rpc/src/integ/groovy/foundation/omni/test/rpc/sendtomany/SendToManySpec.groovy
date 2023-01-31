package foundation.omni.test.rpc.sendtomany

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.OmniOutput
import org.consensusj.jsonrpc.JsonRpcStatusException
import org.junit.jupiter.api.Assumptions

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
        sendTx.totalAmount == 0.00000028.divisible
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
        sendTx.totalAmount == 0.00000036.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000001.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000002.divisible
        omniGetBalance(otherAddress3, CurrencyID.OMNI).balance == 0.00000003.divisible
        omniGetBalance(otherAddress4, CurrencyID.OMNI).balance == 0.00000004.divisible
        omniGetBalance(otherAddress5, CurrencyID.OMNI).balance == 0.00000005.divisible
        omniGetBalance(otherAddress6, CurrencyID.OMNI).balance == 0.00000006.divisible
        omniGetBalance(otherAddress7, CurrencyID.OMNI).balance == 0.00000007.divisible
        omniGetBalance(otherAddress8, CurrencyID.OMNI).balance == 0.00000008.divisible
    }

    def "Send to many: Send 1 willet to 1 of 1 output"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 1 willet to one receiver"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid and the receiver got 1 willet"
        sendTx.valid
        sendTx.totalAmount == 0.00000001.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000001.divisible
    }

    def "Send to many: Send more than one willet to one receiver"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000101.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 101 willets to one receiver"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid and the receiver got 101 willets"
        sendTx.valid
        sendTx.totalAmount == 0.00000101.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000101.divisible
    }

    def "Send to many: Send 10 willets to 1st of 2 outputs, 1 to 2nd"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000010.divisible),
                new OmniOutput(otherAddress2, 0.00000001.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 11 willets to two receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid and the first receiver got 10, the second 1 willet"
        sendTx.valid
        sendTx.totalAmount == 0.00000011.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000010.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000001.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000011.divisible
    }

    def "Send to many: Send 1 willet to 1st of 2 outputs, 11 to 2nd"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress2, 0.00000011.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 11 willets to two receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid and the first receiver got 1, the second 11 willets"
        sendTx.valid
        sendTx.totalAmount == 0.00000012.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000001.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000011.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000012.divisible
    }

    def "Send to many: Send 100000000 willet sto 1 of 1 output"() {
        when:
        def actorAddress = createFundedAddress(startBTC, 1.divisible)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 1.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 1 Omni to one receiver"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid and the receiver got 1 Omni"
        sendTx.valid
        sendTx.totalAmount == 1.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 1.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == 0.divisible
    }

    def "Send to many: Send 3, 2, 1 willets to 3 of 3 outputs in order"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress
        def otherAddress3 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress2, 0.00000002.divisible),
                new OmniOutput(otherAddress3, 0.00000003.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 6 willets to three receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid"
        sendTx.valid
        sendTx.totalAmount == 0.00000006.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000001.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000002.divisible
        omniGetBalance(otherAddress3, CurrencyID.OMNI).balance == 0.00000003.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000006.divisible
    }

    def "Send to many: Send 3, 2, 1 willets to 3 of 3 outputs in reverse order"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress
        def otherAddress3 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000003.divisible),
                new OmniOutput(otherAddress2, 0.00000002.divisible),
                new OmniOutput(otherAddress3, 0.00000001.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 6 willets to three receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid"
        sendTx.valid
        sendTx.totalAmount == 0.00000006.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000003.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000002.divisible
        omniGetBalance(otherAddress3, CurrencyID.OMNI).balance == 0.00000001.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000006.divisible
    }

    def "Send to many: Send 11, 9 willets to 1 of 1 output"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000011.divisible),
                new OmniOutput(otherAddress1, 0.00000009.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 20 willet to one receiver"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid"
        sendTx.valid
        sendTx.totalAmount == 0.00000020.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000020.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000020.divisible
    }

    def "Send to many: Send 11, 10, 9 willets to 1 of 1 output"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000011.divisible),
                new OmniOutput(otherAddress1, 0.00000010.divisible),
                new OmniOutput(otherAddress1, 0.00000009.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 30 willets to one receiver"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid"
        sendTx.valid
        sendTx.totalAmount == 0.00000030.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000030.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000030.divisible
    }

    def "Send to many: Send 11, 10, 9 willets to mixed outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000009.divisible),
                new OmniOutput(otherAddress2, 0.00000011.divisible),
                new OmniOutput(otherAddress2, 0.00000010.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 30 willets to two receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid"
        sendTx.valid
        sendTx.totalAmount == 0.00000030.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000009.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000021.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000030.divisible
    }

    def "Send to many: Send 9, 10, 11 willets to mixed outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000009.divisible),
                new OmniOutput(otherAddress1, 0.00000010.divisible),
                new OmniOutput(otherAddress2, 0.00000010.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 29 willets to two receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid"
        sendTx.valid
        sendTx.totalAmount == 0.00000029.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000019.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000010.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000029.divisible
    }

    def "Send to many: Send 12, 13, 14 willets to 2 of 2 outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000012.divisible),
                new OmniOutput(otherAddress2, 0.00000013.divisible),
                new OmniOutput(otherAddress1, 0.00000014.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 39 willets to two receivers"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "the transaction is valid"
        sendTx.valid
        sendTx.totalAmount == 0.00000039.divisible
        omniGetBalance(otherAddress1, CurrencyID.OMNI).balance == 0.00000026.divisible
        omniGetBalance(otherAddress2, CurrencyID.OMNI).balance == 0.00000013.divisible
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni - 0.00000039.divisible
    }

    def "Send to many: Send multiple within in multiple blocks"() {
        when:
        def senderAddress1 = createFundedAddress(startBTC, 0.00000005.divisible)
        def senderAddress2 = createFundedAddress(startBTC, 0.00000000.divisible)

        def mapping1 = [
                new OmniOutput(senderAddress2, 0.00000003.divisible),
        ]

        def mapping2 = [
                new OmniOutput(senderAddress1, 0.00000001.divisible),
        ]

        def mapping3 = [
                new OmniOutput(senderAddress2, 0.00000003.divisible),
        ]

        then:
        omniGetBalance(senderAddress1, CurrencyID.OMNI).balance == 0.00000005
        omniGetBalance(senderAddress2, CurrencyID.OMNI).balance == 0.00000000

        when: "sending 5 willets back and forth"
        def sendTxid1 = omniSendToMany(senderAddress1, CurrencyID.OMNI, mapping1)
        generateBlocks(1)
        def sendTxid2 = omniSendToMany(senderAddress2, CurrencyID.OMNI, mapping2)
        generateBlocks(1)
        def sendTxid3 = omniSendToMany(senderAddress1, CurrencyID.OMNI, mapping3)
        generateBlocks(1)
        def sendTx1 = omniGetTransaction(sendTxid1)
        def sendTx2 = omniGetTransaction(sendTxid2)
        def sendTx3 = omniGetTransaction(sendTxid3)

        then: "the transaction is valid"
        sendTx1.valid
        sendTx2.valid
        sendTx3.valid
        omniGetBalance(senderAddress1, CurrencyID.OMNI).balance == 0.00000000.divisible
        omniGetBalance(senderAddress2, CurrencyID.OMNI).balance == 0.00000005.divisible
    }

    def "Send to many: Send non-existent property id"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 1.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending 1 non-existing token to one receiver"
        def sendTxid = omniSendToMany(actorAddress, new CurrencyID(777), mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Property identifier does not exist"
    }

    def "Send to many: Send property id 0"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 1.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni.numberValue()

        when: "sending token id 0 to one receiver"
        def sendTxid = omniSendToMany(actorAddress, new CurrencyID(0), mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Property identifier is out of range"
    }

    def "Send to many: Send with zero balance"() {
        when:
        def actorAddress = createFundedAddress(startBTC, 0.divisible)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 1.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == 0.divisible

        when: "sending without having a balance"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Sender has insufficient balance"
    }

    def "Send to many: Send more than available to 1 output"() {
        when:
        def actorAddress = createFundedAddress(startBTC, 0.00000005.divisible)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress1, 0.00000007.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == 0.00000005.divisible

        when: "sending without having a balance"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Sender has insufficient balance"
    }

    def "Send to many: Send more than available to 2 outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, 0.00000005.divisible)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress2, 0.00000007.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == 0.00000005.divisible

        when: "sending without having a balance"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Sender has insufficient balance"
    }

    def "Send to many: Send more than available to 4 outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, 0.00000010.divisible)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress
        def otherAddress3 = newAddress
        def otherAddress4 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress2, 0.00000002.divisible),
                new OmniOutput(otherAddress3, 0.00000077.divisible),
                new OmniOutput(otherAddress4, 0.00000004.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == 0.00000010.divisible

        when: "sending without having a balance"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Sender has insufficient balance"
    }

    def "Send to many: Send 0 willets to 1 of 1 output"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000000.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni

        when: "sending zero tokens"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Invalid amount"
    }

    def "Send to many: Send 0, 1 willets to 2 of 2 outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000000.divisible),
                new OmniOutput(otherAddress2, 0.00000001.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni

        when: "sending zero tokens"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Invalid amount"
    }

    def "Send to many: Send 1, 0 willets to 2 of 2 outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress2, 0.00000000.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni

        when: "sending zero tokens"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Invalid amount"
    }

    def "Send to many: Send 0, 1 willets to 1 of 1 output"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000000.divisible),
                new OmniOutput(otherAddress1, 0.00000001.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni

        when: "sending zero tokens"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Invalid amount"
    }

    def "Send to many: Send 1, 0 willets to 1 of 1 outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress1, 0.00000000.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni

        when: "sending zero tokens"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Invalid amount"
    }

    def "Send to many: Send 1, 2, 3, 0, 5 willets to 5 of 5 outputs"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddress1 = newAddress
        def otherAddress2 = newAddress
        def otherAddress3 = newAddress
        def otherAddress4 = newAddress
        def otherAddress5 = newAddress

        def mapping = [
                new OmniOutput(otherAddress1, 0.00000001.divisible),
                new OmniOutput(otherAddress2, 0.00000002.divisible),
                new OmniOutput(otherAddress3, 0.00000003.divisible),
                new OmniOutput(otherAddress4, 0.00000000.divisible),
                new OmniOutput(otherAddress5, 0.00000005.divisible),
        ]

        then:
        omniGetBalance(actorAddress, CurrencyID.OMNI).balance == startOmni

        when: "sending zero tokens"
        def sendTxid = omniSendToMany(actorAddress, CurrencyID.OMNI, mapping)
        generateBlocks(1)
        def sendTx = omniGetTransaction(sendTxid)

        then: "an exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Invalid amount"
    }

    def "Send to many with 101 receivers with Class B encoding"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startOmni)
        def otherAddressA = newAddress
        def otherAddressB = newAddress

        ArrayList<OmniOutput> mapping = []
        for (def i = 0; i < 100; i++) {
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
        sendTx.totalAmount == 0.00000107.divisible
        omniGetBalance(otherAddressA, CurrencyID.TOMNI).balance == 0.00000100.divisible
        omniGetBalance(otherAddressB, CurrencyID.TOMNI).balance == 0.00000007.divisible
    }

    def setupSpec() {
        if (!commandExists("omni_sendtomany")) {
            Assumptions.abort('The client has no "omni_sendtomany" command')
        }
    }

}
