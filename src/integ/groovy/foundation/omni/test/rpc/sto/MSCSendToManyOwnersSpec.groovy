package foundation.omni.test.rpc.sto
import com.google.bitcoin.core.Address
import com.msgilligan.bitcoin.BTC
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import spock.lang.Unroll

import static foundation.omni.CurrencyID.MSC

/**
 *
 */
class MSCSendToManyOwnersSpec extends BaseRegTestSpec {

    def dryRun(Integer maxN) {
        println "-----------------------------------------------------------------------"
        println String.format("Send to %d owers", maxN)
        println "-----------------------------------------------------------------------"
        print "\n"

        // Preperation
        def fundingSPT = ((maxN * (maxN + 1)) / 2) * 1.00000001
        def actorSPT = ((maxN * (maxN + 1)) / 2) * 0.00000001
        def actorMSC = maxN * 0.00000001

        // Create actor
        def actorAddress = createFundedAddress(1.0, actorMSC)

        // Create property
        def numberOfTokens = BTC.btcToSatoshis(fundingSPT)
        def fundingTxid = createProperty(actorAddress, Ecosystem.MSC, PropertyType.DIVISIBLE, numberOfTokens.longValue())
        generateBlock()

        println String.format("Creating a new divisible property with %s units ...", fundingSPT.toPlainString())

        // Get property identifier
        def fundingTx = getTransactionMP(fundingTxid)
        def currencySPT = new CurrencyID(fundingTx.propertyid)

        // Check funding balances of actor
        def startingBalanceMSC = getbalance_MP(actorAddress, MSC)
        def startingBalanceSPT = getbalance_MP(actorAddress, currencySPT)

        print "\n"
        println String.format("The actor was funded with: %s MSC", startingBalanceMSC.balance.toPlainString())
        println String.format("The actor was funded with: %s SPT", startingBalanceSPT.balance.toPlainString())
        print "\n"

        // Create owners
        def owners = [] as Map<Integer, Address>
        def ownerAddresses = [] as List<Address>

        // Create addresses for owners
        for (n in 1..maxN) {
            ownerAddresses << newAddress
        }

        // Sort addresses to avoid something strange
        ownerAddresses.sort { it.toString() }

        // Fund owners
        for (n in 1..maxN) {
            BigDecimal starting = n * 1.0
            owners[n] = ownerAddresses[n-1]
            send_MP(actorAddress, owners[n], currencySPT, starting)
            println String.format("Sending %s SPT to owner #%d ...", starting.toPlainString(), n)
        }
        generateBlock()

        // Check starting balances of actor
        def reallyBalanceMSC = getbalance_MP(actorAddress, MSC)
        def reallyBalanceSPT = getbalance_MP(actorAddress, currencySPT)

        print "\n"
        println String.format("The actor now has: %s MSC and should have %s MSC",
                reallyBalanceMSC.balance.toPlainString(), actorMSC.toPlainString())
        println String.format("The actor now has: %s SPT and should have %s SPT",
                reallyBalanceSPT.balance.toPlainString(), actorSPT.toPlainString())


        // Check owner balances
        for (n in 1..maxN) {
            def expectedBalanceOwnerSPT = n * 1.0
            def startingBalanceOwnerSPT = getbalance_MP(owners[n], currencySPT)

            println String.format("Owner #%d starts with: %s SPT and should have: %s SPT %s",
                    n, startingBalanceOwnerSPT.balance.toPlainString(), expectedBalanceOwnerSPT.toPlainString(),
                    (startingBalanceOwnerSPT.balance != expectedBalanceOwnerSPT) ? "<------- FAIL" : "")
        }

        print "\n"
        println String.format("Sending %s SPT to %d owners...", actorSPT.toPlainString(), maxN)
        print "\n"

        // Send to owners
        def stoTxid = sendToOwnersMP(actorAddress, currencySPT, actorSPT)
        generateBlock()

        // Check updated owner balances
        for (n in 1..maxN) {
            def expectedFinalBalanceOwnerSPT = n * 1.00000001
            def finalBalanceOwnerSPT = getbalance_MP(owners[n], currencySPT)

            println String.format("Owner #%d ends up with: %s SPT and should have: %s SPT %s",
                    n, finalBalanceOwnerSPT.balance.toPlainString(), expectedFinalBalanceOwnerSPT.toPlainString(),
                    (finalBalanceOwnerSPT.balance != expectedFinalBalanceOwnerSPT) ? "<------- FAIL" : "")
        }

        // Check final balances of actor
        def finalBalanceMSC = getbalance_MP(actorAddress, MSC)
        def finalBalanceSPT = getbalance_MP(actorAddress, currencySPT)

        print "\n"
        println String.format(
                "The actor ends up with: %s MSC and should have 0.0 MSC %s", finalBalanceMSC.balance.toPlainString(),
                (finalBalanceMSC.balance != 0.0) ? "<------- FAIL" : "")
        println String.format(
                "The actor ends up with: %s SPT and should have 0.0 SPT %s", finalBalanceSPT.balance.toPlainString(),
                (finalBalanceSPT.balance != 0.0) ? "<------- FAIL" : "")
    }

    @Unroll
    def "Send to #maxN owners"() {
        dryRun(maxN)

        // Preperation
        def fundingSPT = ((maxN * (maxN + 1)) / 2) * 1.00000001
        def actorSPT = ((maxN * (maxN + 1)) / 2) * 0.00000001
        def actorMSC = maxN * 0.00000001

        // Create actor
        def actorAddress = createFundedAddress(1.0, actorMSC)

        // Create property
        def numberOfTokens = BTC.btcToSatoshis(fundingSPT)
        def fundingTxid = createProperty(actorAddress, Ecosystem.MSC, PropertyType.DIVISIBLE, numberOfTokens.longValue())
        generateBlock()

        // Get property identifier
        def fundingTx = getTransactionMP(fundingTxid)
        def currencySPT = new CurrencyID(fundingTx.propertyid)

        assert fundingTx.valid == true
        assert fundingTx.confirmations == 1

        // Check funding balances of actor
        def startingBalanceMSC = getbalance_MP(actorAddress, MSC)
        def startingBalanceSPT = getbalance_MP(actorAddress, currencySPT)

        assert startingBalanceMSC.balance == actorMSC
        assert startingBalanceSPT.balance == fundingSPT

        // Create owners
        def owners = [] as Map<Integer, Address>
        def ownerAddresses = [] as List<Address>

        // Create addresses for owners
        for (n in 1..maxN) {
            ownerAddresses << newAddress
        }

        // Sort addresses to avoid something strange
        ownerAddresses.sort { it.toString() }

        // Fund owners
        for (n in 1..maxN) {
            BigDecimal starting = n * 1.0
            owners[n] = ownerAddresses[n-1]
            send_MP(actorAddress, owners[n], currencySPT, starting)
        }
        generateBlock()

        // Check starting balances of actor
        def reallyBalanceMSC = getbalance_MP(actorAddress, MSC)
        def reallyBalanceSPT = getbalance_MP(actorAddress, currencySPT)

        assert reallyBalanceMSC.balance == actorMSC
        assert reallyBalanceSPT.balance == actorSPT

        // Check owner balances
        for (n in 1..maxN) {
            def expectedBalanceOwnerSPT = n * 1.0
            def startingBalanceOwnerSPT = getbalance_MP(owners[n], currencySPT)

            assert startingBalanceOwnerSPT.balance == expectedBalanceOwnerSPT
        }

        // Send to owners
        def stoTxid = sendToOwnersMP(actorAddress, currencySPT, actorSPT)
        generateBlock()

        def stoTx = getTransactionMP(stoTxid)
        assert stoTx.valid == true
        assert stoTx.confirmations == 1


        // Check updated owner balances
        for (n in 1..maxN) {
            def expectedFinalBalanceOwnerSPT = n * 1.00000001
            def finalBalanceOwnerSPT = getbalance_MP(owners[n], currencySPT)

            assert finalBalanceOwnerSPT.balance == expectedFinalBalanceOwnerSPT
        }

        // Check final balances of actor
        def finalBalanceMSC = getbalance_MP(actorAddress, MSC)
        def finalBalanceSPT = getbalance_MP(actorAddress, currencySPT)

        assert finalBalanceMSC.balance == 0.0
        assert finalBalanceSPT.balance == 0.0


        where:
        maxN << [1, 2, 65, 100]
    }

}
