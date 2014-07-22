package org.mastercoin.exodus

import com.msgilligan.bitcoin.rpc.MastercoinClient
import groovy.json.JsonSlurper
import org.mastercoin.BaseMainNetSpec
import org.mastercoin.CurrencyID
import org.mastercoin.MPMainNetParams
import org.mastercoin.MPNetworkParameters
import org.mastercoin.MPRegTestParams
import spock.lang.Shared
import spock.lang.Stepwise

import java.lang.Void as Should

/**
 * User: sean
 * Date: 7/22/14
 * Time: 9:02 AM
 */
@Stepwise
class ExodusFundraiserSpec extends BaseMainNetSpec {
    @Shared
    MPNetworkParameters mpNetParams
    @Shared
    Integer    startHeight
    @Shared
    BigDecimal fundraiserAmountBTC
    @Shared
    BigDecimal extraBTCForTxFees
    @Shared
    String     masterCoinAddress

    void setupSpec() {
        fundraiserAmountBTC = 5000.0
        extraBTCForTxFees = 1.0
        // We can start this test 1 blocks before the exodus
        startHeight = mpNetParams.firstExodusBlock - 1
    }

    Should "Be at a block height before Exodus period starts"() {
        when:
        def curHeight = client.getBlockCount()
        println "Current blockheight: ${curHeight}"

        then:
        client.getBlockCount() <= startHeight
    }

    Should "Generate blocks to just before Exodus start"() {
        given:
        def curHeight = client.getBlockCount()

        when: "we tell Master Core to mine enough blocks to bring us just before Exodus"
        client.setGenerate(true, startHeight-curHeight)

        then: "we are at the expected block"
        startHeight == client.getBlockCount()

    }

    Should "Fund a bitcoin address to use for the fundraiser"() {
        when: "we create a new address and send a some mined coins to it"
        masterCoinAddress = client.getNewAddress()              // Create new Bitcoin/Mastercoin address
        client.sendToAddress(masterCoinAddress, fundraiserAmountBTC+extraBTCForTxFees,
                "Put some mined coins into an address for the fundraiser", "initial Mastercoin address")
        client.setGenerate(true, 1)                             // Generate 1 block
        def curHeight = client.getBlockCount()

        then: "the new address has the correct balance"
        fundraiserAmountBTC+extraBTCForTxFees == client.getReceivedByAddress(masterCoinAddress, 1)

        and: "we've entered the fundraiser period"
        curHeight >= mpNetParams.firstExodusBlock
        curHeight <= mpNetParams.lastExodusBlock
    }

    Should "Buy some Mastercoins by sending to Exodus address"() {
        when: "we send coins to the Exodus address"
        client.sendToAddress(mpNetParams.exodusAddress, fundraiserAmountBTC,
                "Buy some MSC", "Exodus address")
        def blocksToWrite = mpNetParams.postExodusBlock - mpNetParams.firstExodusBlock
        client.setGenerate(true, blocksToWrite)                             // Close the fundraiser
        def mscBalance = client.getbalance_MP(masterCoinAddress, CurrencyID.MSC_VALUE)

        then: "the Exodus address has the correct balance"
        fundraiserAmountBTC == client.getReceivedByAddress(mpNetParams.exodusAddress, 1)

        and: "our BTC/MSC address money leftover for Tx fees"
        extraBTCForTxFees == client.getReceivedByAddress(masterCoinAddress, 1)

        and: "our BTC/MSC address has some MSC"
        mscBalance > 0 // need calculation for proper amount
    }
}
