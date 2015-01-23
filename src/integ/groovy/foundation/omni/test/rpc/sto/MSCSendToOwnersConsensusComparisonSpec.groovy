package foundation.omni.test.rpc.sto

import com.google.bitcoin.core.Address
import foundation.omni.CurrencyID
import foundation.omni.BaseRegTestSpec
import foundation.omni.consensus.ConsensusComparison
import foundation.omni.consensus.ConsensusEntry
import foundation.omni.consensus.ConsensusSnapshot
import foundation.omni.consensus.ConsensusTool
import foundation.omni.consensus.MasterCoreConsensusTool
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Unroll


/**
 * Experiment with using Spock data-driven testing and the ConsensusComparison
 * for STO testing.
 *
 * In this test an amount of 100.0 MSC is sent to all MSC owners and it is
 * confirmed that the balance of the recipients really increased.
 *
 * Ignored until Github Issue #26 is resolved
 * https://github.com/msgilligan/bitcoin-spock/issues/26
 * (Or maybe this test is redundant.)
 */
@Ignore()
class MSCSendToOwnersConsensusComparisonSpec extends BaseRegTestSpec {
    @Shared
    ConsensusTool consensusTool

    @Shared
    ConsensusComparison comparison

    @Shared
    Address fundedAddress

    def setupSpec() {
        // Run once before all tests in this Spec
        consensusTool = new MasterCoreConsensusTool(client)
        def startingBTC = 10.0
        def startingMSC = 1000.0
        def amountSent = 100.0
        fundedAddress = createFundedAddress(startingBTC, startingMSC)
        def currencyID = CurrencyID.TMSC
        def startingPropBal = getbalance_MP(fundedAddress, currencyID).balance
        ConsensusSnapshot startSnap = consensusTool.getConsensusSnapshot(currencyID)
        sendToOwnersMP(fundedAddress, currencyID, amountSent)
        generateBlock()
        ConsensusSnapshot endSnap = consensusTool.getConsensusSnapshot(currencyID)
        comparison = new ConsensusComparison(startSnap, endSnap)
    }

    def setup() {
        // Run before each test
    }

    @Unroll
    def "#address #after >= #before" (String address, ConsensusEntry before, ConsensusEntry after) {
        expect:
        (address != fundedAddress.toString()) && (after.balance >= before.balance) ||
                (address == fundedAddress.toString()) && (after.balance < before.balance)

        where:
        [address, before, after] << comparison
    }

}