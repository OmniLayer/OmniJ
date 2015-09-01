package foundation.omni.test.rpc.reorgs

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
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
        def amountToCreate = new BigDecimal("153")

        def txidCreation = createProperty(senderAddress, ecosystem, propertyType, amountToCreate.longValue())
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
        omniGetBalance(senderAddress, currencyID).balance == amountToCreate
    }

}
