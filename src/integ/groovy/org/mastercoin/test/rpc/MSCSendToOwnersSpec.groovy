package org.mastercoin.test.rpc

import org.mastercoin.BaseRegTestSpec
import org.mastercoin.consensus.ConsensusComparison
import org.mastercoin.consensus.ConsensusSnapshot
import org.mastercoin.consensus.ConsensusTool
import org.mastercoin.consensus.MasterCoreConsensusTool
import spock.lang.Shared
import spock.lang.Unroll

/**
 *
 */
class MSCSendToOwnersSpec extends BaseRegTestSpec {
    final static BigDecimal stoFeePerAddress = 0.00000001

    @Shared
    ConsensusTool consensusTool

    @Shared
    ConsensusComparison comparison

    def setup() {
        consensusTool = new MasterCoreConsensusTool(client)
    }

    def "Send to owners calculates correct fees"() {
        setup:
        def startingBTC = 10.0
        def startingMSC = 1000
        def amountSent = 100
        def fundedAddress = createFundedAddress(startingBTC, startingMSC)
        def currencyID = org.mastercoin.CurrencyID.TMSC

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
}