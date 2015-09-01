package foundation.omni.test.rpc.sto
import org.bitcoinj.core.Address
import foundation.omni.BaseRegTestSpec
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import spock.lang.Unroll

import static foundation.omni.CurrencyID.MSC

/**
 * Creates several "send to owners" transactions.
 */
class SendToManyOwnersSpec extends BaseRegTestSpec {

    // Print intermediate results
    final static Boolean EXTRA_DEBUG_ROUNDS = false
    // Number of owners to receive tokens
    final static Integer NUM_OWNERS = 70

    final static BigDecimal stoFeePerAddress = 0.00000001
    final static BigDecimal COIN = 100000000.0

    def dryRun(Integer maxN,
        BigDecimal amountStartPerOwner, BigDecimal amountDistributePerOwner, PropertyType propertyType) {
        print "\n"
        println "-----------------------------------------------------------------------"
        println String.format("%s: start with n * %s and send n * %s to %d owners",
                propertyType.toString(), amountStartPerOwner.toPlainString(),
                amountDistributePerOwner.toPlainString(), maxN)
        println "-----------------------------------------------------------------------"
        print "\n"

        // Preperation
        def fundingSPT = ((maxN * (maxN + 1)) / 2) * (amountStartPerOwner + amountDistributePerOwner)
        def actorSPT = ((maxN * (maxN + 1)) / 2) * amountDistributePerOwner
        def actorMSC = maxN * stoFeePerAddress

        // Create actor
        def actorAddress = createFundedAddress(1.0, actorMSC)

        // Create property
        println String.format("Creating a new %s with %s units ...", propertyType.toString(), fundingSPT.toPlainString())
        def currencySPT = fundNewProperty(actorAddress, fundingSPT, propertyType, Ecosystem.MSC)

        // Check funding balances of actor
        def startingBalanceMSC = omniGetBalance(actorAddress, MSC)
        def startingBalanceSPT = omniGetBalance(actorAddress, currencySPT)

        print "\n"
        println String.format("The actor was funded with: %s MSC", startingBalanceMSC.balance.toPlainString())
        println String.format("The actor was funded with: %s SPT", startingBalanceSPT.balance.toPlainString())
        print "\n"

        // Create owners
        def owners = [] as Map<Integer, Address>

        // Fund owners
        for (n in 1..maxN) {
            BigDecimal starting = n * amountStartPerOwner
            owners[n] = newAddress
            omniSend(actorAddress, owners[n], currencySPT, starting)
            println String.format("Sending %s SPT to owner #%d ...", starting.toPlainString(), n)
            if (n % 500 == 0) {
                generateBlock()
            }
        }
        generateBlock()
        generateBlock()

        // Check starting balances of actor
        def reallyBalanceMSC = omniGetBalance(actorAddress, MSC)
        def reallyBalanceSPT = omniGetBalance(actorAddress, currencySPT)

        print "\n"
        println String.format("The actor now has: %s MSC and should have %s MSC",
                reallyBalanceMSC.balance.toPlainString(), actorMSC.toPlainString())
        println String.format("The actor now has: %s SPT and should have %s SPT",
                reallyBalanceSPT.balance.toPlainString(), actorSPT.toPlainString())


        // Check owner balances
        for (n in 1..maxN) {
            def expectedBalanceOwnerSPT = n * amountStartPerOwner
            def startingBalanceOwnerSPT = omniGetBalance(owners[n], currencySPT)

            println String.format("Owner #%d starts with: %s SPT and should have: %s SPT %s",
                    n, startingBalanceOwnerSPT.balance.toPlainString(), expectedBalanceOwnerSPT.toPlainString(),
                    (startingBalanceOwnerSPT.balance != expectedBalanceOwnerSPT) ? "<------- FAIL" : "")
        }

        print "\n"
        println String.format("Sending %s SPT to %d owners...", actorSPT.toPlainString(), maxN)
        print "\n"

        // Send to owners
        def stoTxid = omniSendSTO(actorAddress, currencySPT, actorSPT)
        generateBlock()

        // Check updated owner balances
        for (n in 1..maxN) {
            def expectedFinalBalanceOwnerSPT = n * (amountStartPerOwner + amountDistributePerOwner)
            def finalBalanceOwnerSPT = omniGetBalance(owners[n], currencySPT)

            println String.format("Owner #%d ends up with: %s SPT and should have: %s SPT %s",
                    n, finalBalanceOwnerSPT.balance.toPlainString(), expectedFinalBalanceOwnerSPT.toPlainString(),
                    (finalBalanceOwnerSPT.balance != expectedFinalBalanceOwnerSPT) ? "<------- FAIL" : "")
        }

        // Check final balances of actor
        def finalBalanceMSC = omniGetBalance(actorAddress, MSC)
        def finalBalanceSPT = omniGetBalance(actorAddress, currencySPT)

        print "\n"
        println String.format(
                "The actor ends up with: %s MSC and should have 0.0 MSC %s", finalBalanceMSC.balance.toPlainString(),
                (finalBalanceMSC.balance != 0.0) ? "<------- FAIL" : "")
        println String.format(
                "The actor ends up with: %s SPT and should have 0.0 SPT %s", finalBalanceSPT.balance.toPlainString(),
                (finalBalanceSPT.balance != 0.0) ? "<------- FAIL" : "")

        assert finalBalanceMSC.balance == 0.0
        assert finalBalanceSPT.balance == 0.0
    }

    @Unroll
    def "#propertyType: start with n * #amountStartPerOwner and send n * #amountDistributePerOwner to #maxN owners"() {
        // Run the tests twice to print intermediate results
        if (EXTRA_DEBUG_ROUNDS) {
            dryRun(maxN, amountStartPerOwner, amountDistributePerOwner, propertyType)
        }

        // Preperation
        def fundingSPT = ((maxN * (maxN + 1)) / 2) * (amountStartPerOwner + amountDistributePerOwner)
        def actorSPT = ((maxN * (maxN + 1)) / 2) * amountDistributePerOwner
        def actorMSC = maxN * stoFeePerAddress

        // Create actor
        def actorAddress = createFundedAddress(1.0, actorMSC, false)

        // Create property
        def currencySPT = fundNewProperty(actorAddress, fundingSPT, propertyType, Ecosystem.MSC)

        // Check funding balances of actor
        def startingBalanceMSC = omniGetBalance(actorAddress, MSC)
        def startingBalanceSPT = omniGetBalance(actorAddress, currencySPT)
        assert startingBalanceMSC.balance == actorMSC
        assert startingBalanceSPT.balance == fundingSPT

        // Create owners
        def owners = [] as Map<Integer, Address>

        // Fund owners
        for (n in 1..maxN) {
            BigDecimal starting = n * amountStartPerOwner
            owners[n] = newAddress
            omniSend(actorAddress, owners[n], currencySPT, starting)
            if (n % 500 == 0) {
                generateBlock()
            }
        }
        generateBlock()
        generateBlock()

        // Check starting balances of actor
        def reallyBalanceMSC = omniGetBalance(actorAddress, MSC)
        def reallyBalanceSPT = omniGetBalance(actorAddress, currencySPT)
        assert reallyBalanceMSC.balance == actorMSC
        assert reallyBalanceSPT.balance == actorSPT

        // Check owner balances
        for (n in 1..maxN) {
            def expectedBalanceOwnerSPT = n * amountStartPerOwner
            def startingBalanceOwnerSPT = omniGetBalance(owners[n], currencySPT)
            assert startingBalanceOwnerSPT.balance == expectedBalanceOwnerSPT
        }

        // Send to owners
        def stoTxid = omniSendSTO(actorAddress, currencySPT, actorSPT)
        generateBlock()

        def stoTx = omniGetTransaction(stoTxid)
        assert stoTx.valid == true
        assert stoTx.confirmations == 1

        // Check updated owner balances
        for (n in 1..maxN) {
            def expectedFinalBalanceOwnerSPT = n * (amountStartPerOwner + amountDistributePerOwner)
            def finalBalanceOwnerSPT = omniGetBalance(owners[n], currencySPT)
            assert finalBalanceOwnerSPT.balance == expectedFinalBalanceOwnerSPT
        }

        // Check final balances of actor
        def finalBalanceMSC = omniGetBalance(actorAddress, MSC)
        def finalBalanceSPT = omniGetBalance(actorAddress, currencySPT)
        assert finalBalanceMSC.balance == 0.0
        assert finalBalanceSPT.balance == 0.0

        where:
        maxN       | amountStartPerOwner                    | amountDistributePerOwner               | propertyType
        NUM_OWNERS | new BigDecimal("1")                    | new BigDecimal("1")                    | PropertyType.INDIVISIBLE
        NUM_OWNERS | new BigDecimal("1")                    | new BigDecimal("3")                    | PropertyType.INDIVISIBLE
        NUM_OWNERS | new BigDecimal("1")                    | new BigDecimal("100000000")            | PropertyType.INDIVISIBLE
        NUM_OWNERS | new BigDecimal("100000000")            | new BigDecimal("1")                    | PropertyType.INDIVISIBLE
        NUM_OWNERS | new BigDecimal("100000000")            | new BigDecimal("3")                    | PropertyType.INDIVISIBLE
        NUM_OWNERS | new BigDecimal("0.00000001")           | new BigDecimal("1.00000000")           | PropertyType.DIVISIBLE
        NUM_OWNERS | new BigDecimal("0.00000001")           | new BigDecimal("2.00000000")           | PropertyType.DIVISIBLE
        NUM_OWNERS | new BigDecimal("1.00000000")           | new BigDecimal("0.00000001")           | PropertyType.DIVISIBLE
        NUM_OWNERS | new BigDecimal("1.00000000")           | new BigDecimal("0.00000002")           | PropertyType.DIVISIBLE
        NUM_OWNERS | new BigDecimal("1.00000000")           | new BigDecimal("0.50000000")           | PropertyType.DIVISIBLE
        NUM_OWNERS | new BigDecimal("1.00000000")           | new BigDecimal("3.00000000")           | PropertyType.DIVISIBLE
        1          | new BigDecimal("1")                    | new BigDecimal("9223372036854775806")  | PropertyType.INDIVISIBLE
        1          | new BigDecimal("9223372036854775806")  | new BigDecimal("1")                    | PropertyType.INDIVISIBLE
        1          | new BigDecimal("0.00000001")           | new BigDecimal("92233720368.54775806") | PropertyType.DIVISIBLE
        1          | new BigDecimal("92233720368.54775806") | new BigDecimal("0.00000001")           | PropertyType.DIVISIBLE
    }

}
