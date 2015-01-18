package org.mastercoin.test.rpc

import com.msgilligan.bitcoin.rpc.JsonRPCStatusException
import org.mastercoin.BaseRegTestSpec
import org.mastercoin.consensus.ConsensusSnapshot
import org.mastercoin.consensus.ConsensusTool
import org.mastercoin.consensus.MasterCoreConsensusTool
import org.mastercoin.rpc.MPBalanceEntry
import spock.lang.Shared

import static org.mastercoin.CurrencyID.*

/**
 *
 */
class MSCSendToOwnersSpec extends BaseRegTestSpec {
    final static BigDecimal stoFeePerAddress = 0.00000001

    @Shared
    ConsensusTool consensusTool

    def setupSpec() {
        // Run once before all tests in this Spec
        consensusTool = new MasterCoreConsensusTool(client)
    }

    def "STO calculates correct fees for the simple case"() {
        setup:
        def startingBTC = 10.0
        def startingMSC = 1000.0
        def amountSent = 100.0
        def fundedAddress = createFundedAddress(startingBTC, startingMSC)
        def currencyID = TMSC
        def expectedBalance = 0.0

        when: "We Send to Owners"
        def startBalances = getallbalancesforid_MP(currencyID)
        def startBalanceSender = getbalance_MP(fundedAddress, currencyID).balance
        def numberOfHolders = startBalances.size()
        sendToOwnersMP(fundedAddress, currencyID, amountSent)

        and: "We generate a block"
        generateBlock()

        // The fee for each receiver is #stoFeePerAddress
        if (numberOfHolders > 1) {
            def endBalances = getallbalancesforid_MP(currencyID)
            def changedBalances = endBalances - startBalances
            def numberReceivers = changedBalances.size() - 1
            def totalFee = numberReceivers * stoFeePerAddress
            expectedBalance = startBalanceSender - amountSent - totalFee
        } else {
            // There are no other holders, thus no receiver or fee
            expectedBalance = startBalanceSender
        }

        then: "Our balance has been reduced by amount sent + fees"
        getbalance_MP(fundedAddress, currencyID).balance == expectedBalance
    }

    def "STO fails when amount sent is zero"() {
        setup:
        def fundedAddress = createFundedAddress(10.0, 100.0)
        def currencyID = TMSC
        def startBalances = consensusTool.getConsensusSnapshot(currencyID)

        when: "We Send to Owners with amount equal zero"
        sendToOwnersMP(fundedAddress, currencyID, 0.0)

        then: "exception is thrown"
        JsonRPCStatusException e = thrown()
        e.message == "Invalid amount"
        e.responseJson.error.code == -3

        when: "we check balances"
        def endBalances = consensusTool.getConsensusSnapshot(currencyID)

        then: "balances unchanged"
        startBalances == endBalances
    }

}
