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
        def startingMSC = 1000
        def amountSent = 100
        def fundedAddress = createFundedAddress(startingBTC, startingMSC)
        def currencyID = TMSC

        when: "We Send to Owners"
        def startingPropBal = getbalance_MP(fundedAddress, currencyID).balance
        sendToOwnersMP(fundedAddress, currencyID, amountSent)

        and: "we generate a block"
        generateBlock()
        def balances = getallbalancesforid_MP(currencyID)
        def numberDest = balances.size() - 1
        def totalFee = numberDest * stoFeePerAddress
        def expectedBalance = (numberDest == 0) ? startingPropBal : startingPropBal - amountSent - totalFee

        then: "Our balance has been reduced by amount sent + fees"
        getbalance_MP(fundedAddress, currencyID).balance == expectedBalance
    }

    def "STO fails when amount sent is zero"() {
        setup:
        def fundedAddress = createFundedAddress(10, 100)
        def currencyID = TMSC
        def startBalances = consensusTool.getConsensusSnapshot(currencyID)

        when: "We Send to Owners with amount equal zero"
        sendToOwnersMP(fundedAddress, currencyID, 0)

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