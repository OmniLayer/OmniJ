package foundation.omni.test.rpc.reorgs

import foundation.omni.CurrencyID

class SimpleSendReorgSpec extends BaseReorgSpec {

    final static BigDecimal sendAmount = 0.1

    def "After invalidating a simple send, the send transaction is invalid"()
    {
        given:
        def receiverAddress = newAddress
        def senderAddress = createFundedAddress(startBTC, startMSC)
        def blockCountBeforeSend = getBlockCount()

        when: "broadcasting and confirming a simple send"
        def txid = send_MP(senderAddress, receiverAddress, CurrencyID.MSC, sendAmount)
        def blockHashOfSend = generateAndGetBlockHash()

        then: "the transaction is valid"
        checkTransactionValidity(txid)

        when: "invalidating the block with the send transaction"
        invalidateBlock(blockHashOfSend)

        then: "the send transaction is no longer confirmed"
        getBlockCount() == blockCountBeforeSend
        getTransaction(txid).confirmations < 1

        when: "a new block is mined"
        clearMemPool()
        generateBlock()

        then: "the send transaction is no longer valid"
        !checkTransactionValidity(txid)
    }

    def "After invalidating a simple send, the original balances are restored"()
    {
        def blockHashBeforeFunding = generateAndGetBlockHash()

        def receiverAddress = newAddress
        def senderAddress = createFundedAddress(startBTC, startMSC)

        def balanceBeforeSendActor = getbalance_MP(senderAddress, CurrencyID.MSC)
        def balanceBeforeSendReceiver = getbalance_MP(receiverAddress, CurrencyID.MSC)

        when: "broadcasting and confirming a simple send"
        def txid = send_MP(senderAddress, receiverAddress, CurrencyID.MSC, sendAmount)
        def blockHashOfSend = generateAndGetBlockHash()

        then: "the transaction is valid and the tokens were transferred"
        checkTransactionValidity(txid)
        getbalance_MP(senderAddress, CurrencyID.MSC).balance == balanceBeforeSendActor.balance - sendAmount
        getbalance_MP(receiverAddress, CurrencyID.MSC).balance == balanceBeforeSendReceiver.balance + sendAmount

        when: "invalidating the block with the send transaction and after a new block is mined"
        invalidateBlock(blockHashOfSend)
        clearMemPool()
        generateBlock()

        then: "the send transaction is no longer valid and the balances before the send are restored"
        !checkTransactionValidity(txid)
        getbalance_MP(senderAddress, CurrencyID.MSC) == balanceBeforeSendActor
        getbalance_MP(receiverAddress, CurrencyID.MSC) == balanceBeforeSendReceiver

        when: "rolling back all blocks until before the initial funding"
        invalidateBlock(blockHashBeforeFunding)
        clearMemPool()
        generateBlock()

        then: "the actors have zero balances"
        getbalance_MP(senderAddress, CurrencyID.MSC).balance == 0.0
        getbalance_MP(receiverAddress, CurrencyID.MSC).balance == 0.0
    }

}
