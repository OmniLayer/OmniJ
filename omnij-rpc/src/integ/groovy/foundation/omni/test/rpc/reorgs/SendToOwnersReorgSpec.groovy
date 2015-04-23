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
        def txid = sendToOwnersMP(senderAddress, CurrencyID.TMSC, sendAmount)
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
        def txCreation = getTransactionMP(txidCreation)
        def currencyID = new CurrencyID(txCreation.propertyid as long)

        when: "funding the owners with a new property"
        send_MP(senderAddress, dummyOwnerA, currencyID, new BigDecimal("1"))
        send_MP(senderAddress, dummyOwnerB, currencyID, new BigDecimal("1"))
        send_MP(senderAddress, dummyOwnerC, currencyID, new BigDecimal("1"))
        def blockHashOfOwnerFunding = generateAndGetBlockHash()

        then: "the owners have some balance"
        getbalance_MP(dummyOwnerA, currencyID).balance == new BigDecimal("1")
        getbalance_MP(dummyOwnerB, currencyID).balance == new BigDecimal("1")
        getbalance_MP(dummyOwnerC, currencyID).balance == new BigDecimal("1")

        and: "the sender has less"
        getbalance_MP(senderAddress, currencyID).balance == new BigDecimal("150")

        when: "sending to the owners"
        def txidSTO = sendToOwnersMP(senderAddress, currencyID, new BigDecimal("150"))
        def blockHashOfSend = generateAndGetBlockHash()

        then: "the send to owners transaction is valid"
        checkTransactionValidity(txidSTO)

        and: "the owners received the tokens"
        getbalance_MP(dummyOwnerA, currencyID).balance == new BigDecimal("51")
        getbalance_MP(dummyOwnerB, currencyID).balance == new BigDecimal("51")
        getbalance_MP(dummyOwnerC, currencyID).balance == new BigDecimal("51")

        and: "the sender has no more tokens"
        getbalance_MP(senderAddress, currencyID).balance == new BigDecimal("0")

        when: "invalidating the block and send to owners transaction"
        invalidateBlock(blockHashOfSend)
        clearMemPool()
        generateBlock()

        then: "the send to owners transaction is no longer valid"
        !checkTransactionValidity(txidSTO)

        and: "the owners no longer have the tokens they received"
        getbalance_MP(dummyOwnerA, currencyID).balance == new BigDecimal("1")
        getbalance_MP(dummyOwnerB, currencyID).balance == new BigDecimal("1")
        getbalance_MP(dummyOwnerC, currencyID).balance == new BigDecimal("1")

        and: "the sender has the balance from before the send to owners transaction"
        getbalance_MP(senderAddress, currencyID).balance == new BigDecimal("150")

        when: "rolling back until before the funding of the owners"
        invalidateBlock(blockHashOfOwnerFunding)
        clearMemPool()
        generateBlock()

        then: "the owners have no tokens"
        getbalance_MP(dummyOwnerA, currencyID).balance == new BigDecimal("0")
        getbalance_MP(dummyOwnerB, currencyID).balance == new BigDecimal("0")
        getbalance_MP(dummyOwnerC, currencyID).balance == new BigDecimal("0")

        and: "the sender has the initial amount that was created"
        getbalance_MP(senderAddress, currencyID).balance == amountToCreate
    }

}
