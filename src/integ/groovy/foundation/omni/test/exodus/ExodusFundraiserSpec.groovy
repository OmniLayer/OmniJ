package foundation.omni.test.exodus

import foundation.omni.BaseRegTestSpec
import spock.lang.Ignore

import static foundation.omni.CurrencyID.*
import foundation.omni.OPNetworkParameters
import foundation.omni.OPRegTestParams
import spock.lang.Shared
import spock.lang.Stepwise

/**
 *
 * Test Specification for Initial Exodus Fundraiser
 *
 * This Spec runs @Stepwise which means each test builds on the previous test
 * and the Spec will be aborted upon the first test that fails.
 *
 * User: sean
 * Date: 7/22/14
 * Time: 9:02 AM
 */
@Ignore
@Stepwise
class ExodusFundraiserSpec extends BaseRegTestSpec {
    @Shared
    OPNetworkParameters mpNetParams
    @Shared
    Integer    startHeight
    @Shared
    BigDecimal fundraiserAmountBTC
    @Shared
    BigDecimal extraBTCForTxFees
    @Shared
    String participatingAddress

    void setupSpec() {
        mpNetParams = OPRegTestParams.get()
        fundraiserAmountBTC = 5000.0
        extraBTCForTxFees = 1.0
        // We can start this test 1 blocks before the exodus
        startHeight = mpNetParams.firstExodusBlock - 1
    }

    def "Be at a block height before Exodus crowdsale starts"() {
        expect:
        client.blockCount <= startHeight

        and: "the Exodus address should have a zero balance"
        0.0 == client.getReceivedByAddress(mpNetParams.exodusAddress)
    }

    def "Generate blocks to just before Exodus crowdsale start"() {

        when: "we tell Master Core to mine enough blocks to bring us just before Exodus"
        def curHeight = client.blockCount
        client.generateBlocks(startHeight-curHeight)

        then: "we are at the expected block"
        startHeight == client.blockCount

    }

    def "Fund an address with BTC for sending BTC to Exodus address"() {
        when: "we create a new address and send a some mined coins to it"
        participatingAddress = client.getNewAddress()    // Create new Bitcoin/Mastercoin address
        client.sendToAddress(participatingAddress, fundraiserAmountBTC+extraBTCForTxFees,
                "Put some mined coins into an address for the fundraiser", "Initial Mastercoin address")
        client.generateBlocks(1)
        def curHeight = client.blockCount

        then: "the new address has the correct balance in BTC"
        fundraiserAmountBTC+extraBTCForTxFees == getReceivedByAddress(participatingAddress)

        and: "we've entered the fundraiser period"
        curHeight == startHeight + 1
        curHeight >= mpNetParams.firstExodusBlock
        curHeight <= mpNetParams.lastExodusBlock
    }

    def "Buy some Mastercoins by sending BTC to Exodus address"() {
        when: "we send coins to the Exodus address"
        // TODO: #1 Use an RPC method that allows us to specify participatingAddress as sending address
        // TODO: #2 We need to somehow set participatingAddress as the change address
        // TODO: #3 Account for Early Bird bonus that we should receive as mscBalance
        // TODO: #4 Ensure we're getting at least one time quantum (second?) between blocks
        client.sendToAddress(mpNetParams.exodusAddress, fundraiserAmountBTC,
                "Buy some MSC", "Exodus address")
        def blocksToGen = mpNetParams.postExodusBlock - mpNetParams.firstExodusBlock
        client.generateBlocks(blocksToGen)          // Close the fundraiser
        def mscBalance = client.getbalance_MP(participatingAddress, MSC)

        then: "we are at the 'Post Exodus' Block"
        mpNetParams.postExodusBlock == blockCount

        and: "the Exodus address has the correct balance"
        fundraiserAmountBTC == client.getReceivedByAddress(mpNetParams.exodusAddress)

        and: "our BTC/MSC address money leftover for Tx fees"
        extraBTCForTxFees == client.getReceivedByAddress(participatingAddress)

        and: "our BTC/MSC address has the correct amount MSC"
        mscBalance >= 100 * fundraiserAmountBTC // need calculation for proper amount
    }
}
