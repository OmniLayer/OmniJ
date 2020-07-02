package foundation.omni.test.rpc.reorgs

import foundation.omni.CurrencyID

class SimpleSendReorgSpec extends BaseReorgSpec {

    final static sendAmount = 0.1.divisible

    def "After invalidating a simple send, the send transaction is invalid"()
    {
        given:
        def receiverAddress = newAddress
        def senderAddress = createFundedAddress(startBTC, startOMNI)
        def blockCountBeforeSend = getBlockCount()

        when: "broadcasting and confirming a simple send"
        def txid = omniSend(senderAddress, receiverAddress, CurrencyID.OMNI, sendAmount)
        def blockHashOfSend = generateAndGetBlockHash()

        then: "the transaction is valid"
        checkTransactionValidity(txid)

        when: "invalidating the block with the send transaction"
        invalidateBlock(blockHashOfSend)
        delayAfterInvalidate()  // Sleep for a little while to avoid `ProcessNewBlock, block not accepted` (duplicate block)

        then: "the send transaction is no longer confirmed"
        getBlockCount() == blockCountBeforeSend
        getTransaction(txid).confirmations < 1

        when: "a new block is mined"
        clearMemPool()
        generateBlocks(1)

        then: "the send transaction is no longer valid"
        !checkTransactionValidity(txid)
    }

    def "After invalidating a simple send, the original balances are restored"()
    {
        def blockHashBeforeFunding = generateAndGetBlockHash()

        def receiverAddress = newAddress
        def senderAddress = createFundedAddress(startBTC, startOMNI)

        def balanceBeforeSendActor = omniGetBalance(senderAddress, CurrencyID.OMNI)
        def balanceBeforeSendReceiver = omniGetBalance(receiverAddress, CurrencyID.OMNI)

        when: "broadcasting and confirming a simple send"
        def txid = omniSend(senderAddress, receiverAddress, CurrencyID.OMNI, sendAmount)
        def blockHashOfSend = generateAndGetBlockHash()

        then: "the transaction is valid and the tokens were transferred"
        checkTransactionValidity(txid)
        omniGetBalance(senderAddress, CurrencyID.OMNI).balance == balanceBeforeSendActor.balance - sendAmount.numberValue()
        omniGetBalance(receiverAddress, CurrencyID.OMNI).balance == balanceBeforeSendReceiver.balance + sendAmount.numberValue()

        when: "invalidating the block with the send transaction and after a new block is mined"
        invalidateBlock(blockHashOfSend)
        clearMemPool()
        delayAfterInvalidate()  // Sleep for a little while to avoid `ProcessNewBlock, block not accepted` (duplicate block)
        generateBlocks(1)

        then: "the send transaction is no longer valid and the balances before the send are restored"
        !checkTransactionValidity(txid)
        omniGetBalance(senderAddress, CurrencyID.OMNI) == balanceBeforeSendActor
        omniGetBalance(receiverAddress, CurrencyID.OMNI) == balanceBeforeSendReceiver

        when: "rolling back all blocks until before the initial funding"
        invalidateBlock(blockHashBeforeFunding)
        clearMemPool()
        longerDelayAfterInvalidate()  // Sleep for a LONGER while to avoid `ProcessNewBlock, block not accepted` (duplicate block)
        generateBlocks(1)

        then: "the actors have zero balances"
        omniGetBalance(senderAddress, CurrencyID.OMNI).balance == 0.0
        omniGetBalance(receiverAddress, CurrencyID.OMNI).balance == 0.0
    }

}
