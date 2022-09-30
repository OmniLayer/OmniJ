package foundation.omni.test.rpc.sto

import foundation.omni.tx.RawTxBuilder
import org.consensusj.jsonrpc.JsonRpcStatusException
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.consensus.ConsensusTool
import foundation.omni.consensus.OmniCoreConsensusTool
import spock.lang.Shared

import static foundation.omni.CurrencyID.*

/**
 *
 */
class SendToOwnersSpec extends BaseRegTestSpec {
    final static BigDecimal stoFeePerAddress = 0.00000001

    @Shared
    ConsensusTool consensusTool

    def setupSpec() {
        // Run once before all tests in this Spec
        consensusTool = new OmniCoreConsensusTool(client)
    }

    def "STO calculates correct fees for the simple case"() {
        setup:
        def startingBTC = 10.btc
        def startingMSC = 1000.divisible
        def amountSent = 100.divisible
        def fundedAddress = createFundedAddress(startingBTC, startingMSC)
        def currencyID = TOMNI
        def expectedBalance = 0.0

        when: "We Send to Owners"
        def startBalances = omniGetAllBalancesForId(currencyID)
        def startBalanceSender = omniGetBalance(fundedAddress, currencyID).balance
        def numberOfHolders = startBalances.size()
        omniSendSTO(fundedAddress, currencyID, amountSent)

        and: "We generate a block"
        generateBlocks(1)

        // The fee for each receiver is #stoFeePerAddress
        if (numberOfHolders > 1) {
            def endBalances = omniGetAllBalancesForId(currencyID)
            def changedBalances = endBalances - startBalances
            def numberReceivers = changedBalances.size() - 1
            def totalFee = numberReceivers * stoFeePerAddress
            expectedBalance = startBalanceSender - amountSent.numberValue() - totalFee
        } else {
            // There are no other holders, thus no receiver or fee
            expectedBalance = startBalanceSender
        }

        then: "Our balance has been reduced by amount sent + fees"
        omniGetBalance(fundedAddress, currencyID).balance == expectedBalance
    }

    def "STO fails when amount sent is zero"() {
        setup:
        def fundedAddress = createFundedAddress(10.0.btc, 100.0.divisible)
        def currencyID = TOMNI
        def startBalances = consensusTool.getConsensusSnapshot(currencyID)

        when: "We Send to Owners with amount equal zero"
        omniSendSTO(fundedAddress, currencyID, 0.divisible)

        then: "exception is thrown"
        JsonRpcStatusException e = thrown()
        e.message == "Invalid amount"
        e.responseJson.error.code == -3

        when: "we make a block"
        generateBlocks(1)

        and: "we check balances"
        def endBalances = consensusTool.getConsensusSnapshot(currencyID)

        then: "balances unchanged"
        endBalances.blockHeight == startBalances.blockHeight + 1
        endBalances.currencyID == startBalances.currencyID
        endBalances.sourceType == startBalances.sourceType
        endBalances.entries == startBalances.entries
    }

    def "The generated hex-encoded transaction matches a valid reference transaction"() {
        given:
        RawTxBuilder builder = new RawTxBuilder()
        def txHex = builder.createSendToOwnersHex(new CurrencyID(6L), 100000000000.indivisible)
        def expectedHex = "0000000300000006000000174876e800"

        expect:
        txHex == expectedHex
    }

}
