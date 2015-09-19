package foundation.omni.test.rpc.reorgs

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.OmniValue
import foundation.omni.PropertyType

class SendToOwnersReorgSpec extends BaseReorgSpec {

    def "After invalidating a send to owners transaction, the transaction is invalid"()
    {
        given:
        def sendAmount = new BigDecimal("0.1")
        def senderAddress = createFundedAddress(startBTC, startMSC)
        def dummyOwnerAddress = createFundedAddress(startBTC, startMSC)

        when: "broadcasting and confirming a send to owners transaction"
        def txid = omniSendSTO(senderAddress, CurrencyID.TMSC, sendAmount)
        def blockHashOfSend = generateAndGetBlockHash()

        then: "the transaction is valid"
        checkTransactionValidity(txid)

        when: "invalidating the block and send to owners transaction"
        invalidateBlock(blockHashOfSend)
        clearMemPool()
        generateBlock()

        then: "the send transaction is no longer valid"
        !checkTransactionValidity(txid)
    }

    def "After invalidating a send to owners transaction, the original balances are restored"()
    {
        given:
        def senderAddress = createFundedAddress(startBTC, startMSC)
        def dummyOwnerA = newAddress
        def dummyOwnerB = newAddress
        def dummyOwnerC = newAddress

        def ecosystem = Ecosystem.MSC
        def propertyType = PropertyType.INDIVISIBLE
        def amountToCreate = OmniValue.of(153, propertyType)

        def txidCreation = createProperty(senderAddress, ecosystem, amountToCreate)
        generateBlock()
        def txCreation = omniGetTransaction(txidCreation)
        def currencyID = new CurrencyID(txCreation.propertyid as long)

        when: "funding the owners with a new property"
        omniSend(senderAddress, dummyOwnerA, currencyID, new BigDecimal("1"))
        omniSend(senderAddress, dummyOwnerB, currencyID, new BigDecimal("1"))
        omniSend(senderAddress, dummyOwnerC, currencyID, new BigDecimal("1"))
        def blockHashOfOwnerFunding = generateAndGetBlockHash()

        then: "the owners have some balance"
        omniGetBalance(dummyOwnerA, currencyID).balance == new BigDecimal("1")
        omniGetBalance(dummyOwnerB, currencyID).balance == new BigDecimal("1")
        omniGetBalance(dummyOwnerC, currencyID).balance == new BigDecimal("1")

        and: "the sender has less"
        omniGetBalance(senderAddress, currencyID).balance == new BigDecimal("150")

        when: "sending to the owners"
        def txidSTO = omniSendSTO(senderAddress, currencyID, new BigDecimal("150"))
        def blockHashOfSend = generateAndGetBlockHash()

        then: "the send to owners transaction is valid"
        checkTransactionValidity(txidSTO)

        and: "the owners received the tokens"
        omniGetBalance(dummyOwnerA, currencyID).balance == new BigDecimal("51")
        omniGetBalance(dummyOwnerB, currencyID).balance == new BigDecimal("51")
        omniGetBalance(dummyOwnerC, currencyID).balance == new BigDecimal("51")

        and: "the sender has no more tokens"
        omniGetBalance(senderAddress, currencyID).balance == new BigDecimal("0")

        when: "invalidating the block and send to owners transaction"
        invalidateBlock(blockHashOfSend)
        clearMemPool()
        generateBlock()

        then: "the send to owners transaction is no longer valid"
        !checkTransactionValidity(txidSTO)

        and: "the owners no longer have the tokens they received"
        omniGetBalance(dummyOwnerA, currencyID).balance == new BigDecimal("1")
        omniGetBalance(dummyOwnerB, currencyID).balance == new BigDecimal("1")
        omniGetBalance(dummyOwnerC, currencyID).balance == new BigDecimal("1")

        and: "the sender has the balance from before the send to owners transaction"
        omniGetBalance(senderAddress, currencyID).balance == new BigDecimal("150")

        when: "rolling back until before the funding of the owners"
        invalidateBlock(blockHashOfOwnerFunding)
        clearMemPool()
        generateBlock()

        then: "the owners have no tokens"
        omniGetBalance(dummyOwnerA, currencyID).balance == new BigDecimal("0")
        omniGetBalance(dummyOwnerB, currencyID).balance == new BigDecimal("0")
        omniGetBalance(dummyOwnerC, currencyID).balance == new BigDecimal("0")

        and: "the sender has the initial amount that was created"
        omniGetBalance(senderAddress, currencyID).balance == amountToCreate.bigDecimalValue()
    }

    def "Historical STO transactions are not affected by reorganizations"() {
        when:
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def tokenID = fundNewProperty(actorAddress, 100.divisible, Ecosystem.MSC)
        def ownerA = newAddress
        def ownerB = newAddress
        omniSend(actorAddress, ownerA, tokenID, 10.0)
        omniSend(actorAddress, ownerB, tokenID, 10.0)
        generateBlock()

        then:
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == startMSC
        omniGetBalance(actorAddress, tokenID).balance == 100.0 - 20.0
        omniGetBalance(ownerA, tokenID).balance == 10.0
        omniGetBalance(ownerB, tokenID).balance == 10.0

        when: "sending the first STO transaction"
        def firstTxid = omniSendSTO(actorAddress, tokenID, 30.0)
        generateBlock()
        def firstTx = omniGetSTO(firstTxid)

        then: "the transaction is valid"
        firstTx.txid == firstTxid.toString()
        firstTx.sendingaddress == actorAddress.toString()
        firstTx.valid
        firstTx.version == 0
        firstTx.type_int == 3
        firstTx.propertyid == tokenID.getValue()
        firstTx.divisible
        firstTx.amount as BigDecimal == 30.0
        firstTx.totalstofee as BigDecimal == 0.00000002
        firstTx.recipients.size == 2
        firstTx.recipients.any { it.address == ownerA.toString() }
        firstTx.recipients.any { it.address == ownerB.toString() }
        firstTx.recipients.every { it.amount as BigDecimal == 15.0 } // 30.0 for two owners

        and: "two owners received tokens"
        omniGetBalance(ownerA, tokenID).balance == 10.0 + 15.0
        omniGetBalance(ownerB, tokenID).balance == 10.0 + 15.0

        and: "the actor was charged"
        omniGetBalance(actorAddress, tokenID).balance == 100.0 - 20.0 - 30.0
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == startMSC - (2 * 0.00000001)

        when: "sending a second STO transaction"
        def secondTxid = omniSendSTO(actorAddress, tokenID, 30.0)
        def blockHashOfSecond = generateAndGetBlockHash()

        and: "invalidating the block with the second STO transaction"
        invalidateBlock(blockHashOfSecond)
        clearMemPool()
        generateBlock()
        def secondOrphanedTx = omniGetTransaction(secondTxid)

        then: "the transaction is not valid"
        !secondOrphanedTx.valid
        // secondOrphanedTx.confirmations == -1 TODO: activate after Omni Core adjustment

        when: "creating a third STO transaction"
        def thirdTxid = omniSendSTO(actorAddress, tokenID, 50.0)
        generateBlock()
        def thirdTx = omniGetSTO(thirdTxid)

        then: "the third STO transaction is valid"
        thirdTx.valid
        thirdTx.amount as BigDecimal == 50.0
        thirdTx.totalstofee as BigDecimal == 0.00000002
        thirdTx.recipients.size == 2
        thirdTx.recipients.any { it.address == ownerA.toString() }
        thirdTx.recipients.any { it.address == ownerB.toString() }
        thirdTx.recipients.every { it.amount as BigDecimal == 25.0 } // 50.0 for two owners

        when: "checking the first STO transaction once more"
        def firstTxNow = omniGetSTO(firstTxid)

        then: "the information for the first transaction is still the same"
        firstTxNow.valid
        firstTx.amount as BigDecimal == 30.0
        firstTx.totalstofee as BigDecimal == 0.00000002
        firstTx.recipients.size == 2
        firstTx.recipients.any { it.address == ownerA.toString() }
        firstTx.recipients.any { it.address == ownerB.toString() }
        firstTx.recipients.every { it.amount as BigDecimal == 15.0 } // 30.0 for two owners

        and: "the final balances as expected"
        omniGetBalance(actorAddress, tokenID).balance == 100.0 - 20.0 - 30.0 - 50.0
        omniGetBalance(ownerA, tokenID).balance == 10.0 + 15.0 + 25.0 // initial + first STO + third STO
        omniGetBalance(ownerB, tokenID).balance == 10.0 + 15.0 + 25.0
        omniGetBalance(actorAddress, CurrencyID.MSC).balance == startMSC - (4 * 0.00000001) // fee for each owner
    }
}
