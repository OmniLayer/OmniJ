package foundation.omni.test.rpc.mdex

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.OmniDivisibleValue
import org.bitcoinj.core.Coin
import org.junit.jupiter.api.Assumptions
import spock.lang.Unroll

/**
 * Specification for the distributed token exchange
 */
class MetaDexSpec extends BaseRegTestSpec {

    final static Coin startBTC = 0.001.btc
    final static OmniDivisibleValue zeroAmount = 0.0.divisible

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/39
     */
    def "Orders are executed, if the effective price is within the accepted range of both"() {
        def actorA = createFundedAddress(startBTC, 0.00000006.divisible, false)
        def actorB = createFundedAddress(startBTC, zeroAmount, false)
        def actorC = createFundedAddress(startBTC, 0.00000001.divisible, false)
        def actorD = createFundedAddress(startBTC, 0.00000001.divisible, false)
        def propertyOMNI = CurrencyID.OMNI
        def propertySPX = fundNewProperty(actorB, 10.divisible, propertyOMNI.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertyOMNI, 0.00000006.divisible , propertySPX, 6.indivisible)
        generateBlocks(1)
        def txidTradeB = omniSendTrade(actorB, propertySPX, 10.indivisible, propertyOMNI, 0.00000001.divisible)
        generateBlocks(1)
        def txidTradeC = omniSendTrade(actorC, propertyOMNI, 0.00000001.divisible, propertySPX, 10.indivisible)
        generateBlocks(1)
        def txidTradeD = omniSendTrade(actorD, propertyOMNI, 0.00000001.divisible, propertySPX, 4.indivisible)
        generateBlocks(1)

        then:
        omniGetTrade(txidTradeA).valid
        omniGetTrade(txidTradeB).valid
        omniGetTrade(txidTradeC).valid
        omniGetTrade(txidTradeD).valid

        and:
        omniGetTrade(txidTradeA).status == "filled"
        omniGetTrade(txidTradeB).status == "filled"
        omniGetTrade(txidTradeC).status == "open"
        omniGetTrade(txidTradeD).status == "filled"

        and:
        omniGetOrderbook(propertyOMNI, propertySPX).size() == 1
        omniGetOrderbook(propertySPX, propertyOMNI).size() == 0

        and:
        omniGetBalance(actorC, propertyOMNI).reserved == 0.00000001

        when: "retrieving additional information about trade C"
        def txTradeC = omniGetTrade(txidTradeC)

        then: "the order is still unmatched"
        txTradeC.txId == txidTradeC
        txTradeC.sendingAddress == actorC
        txTradeC.version == 0
        txTradeC.type_int == 25
        txTradeC.type == "MetaDEx trade"
        txTradeC.propertyIdForSale == propertyOMNI
        txTradeC.propertyIdForSaleIsDivisible
        txTradeC.amountForSale == 0.00000001.divisible
        txTradeC.propertyIdDesired == propertySPX
        txTradeC.amountDesired == 10.indivisible
        txTradeC.amountRemaining == 0.00000001.divisible
        txTradeC.amountToFill == 10.indivisible
        txTradeC.matches.size() == 0
        if (omniGetInfo().omnicoreversion_int < 1100000) {
            assert txTradeC.unitPrice == "0.00000000100000000000000000000000000000000000000000"
        } else {
            assert txTradeC.unitPrice == "1000000000.00000000000000000000000000000000000000000000000000"
        }

        when: "we retrieve trade history for actorA"
        def history = omniGetTradeHistoryForAddress(actorA, 100, propertyOMNI);

        then: "the correct number of transactions are there"
        history.size() == 1

        when:
        def fill1 = history.get(0)
        def matches = fill1.matches

        then:
        matches instanceof List
        matches.size() == 1
    }

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/37
     */
    def "Two orders can match, and both can be partially filled"() {
        def actorA = createFundedAddress(startBTC, zeroAmount, false)
        def actorB = createFundedAddress(startBTC, 0.55.divisible, false)
        def propertyOMNI = CurrencyID.OMNI
        def propertySPX = fundNewProperty(actorA, 25.indivisible, propertyOMNI.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertySPX, 25.indivisible, propertyOMNI, 2.50.divisible)
        generateBlocks(1)
        def txidTradeB = omniSendTrade(actorB, propertyOMNI, 0.55.divisible, propertySPX, 5.indivisible)
        generateBlocks(1)

        then:
        omniGetTransaction(txidTradeA).valid
        omniGetTransaction(txidTradeB).valid

        and:
        omniGetBalance(actorA, propertyOMNI).balance == 0.5
        omniGetBalance(actorA, propertySPX).reserved == 20 as BigDecimal
        omniGetBalance(actorB, propertySPX).balance == 5 as BigDecimal
        omniGetBalance(actorB, propertyOMNI).reserved == 0.05

        when: "retrieving information about trade A"
        def txTradeA = omniGetTrade(txidTradeA)

        then:
        txTradeA.status == "open part filled"

        and:
        txTradeA.propertyIdForSale == propertySPX
        txTradeA.amountForSale == 25.indivisible
        txTradeA.propertyIdDesired == propertyOMNI
        txTradeA.amountDesired == 2.5.divisible
        txTradeA.unitPrice == "0.10000000000000000000000000000000000000000000000000"
        txTradeA.amountRemaining == 20.indivisible
        txTradeA.amountToFill == 2.0.divisible
        txTradeA.matches.size() == 1

        when:
        def tradeMatchA = txTradeA.matches.find { it.txId == txidTradeB }

        then:
        tradeMatchA.address == actorB
        tradeMatchA.amountSold == 5.divisible
        tradeMatchA.amountReceived == 0.5.divisible

        when: "retrieving information about trade B"
        def txTradeB = omniGetTrade(txidTradeB)

        then:
        txTradeB.status == "open part filled"

        and:
        txTradeB.propertyIdForSale == propertyOMNI
        txTradeB.amountForSale == 0.55.divisible
        txTradeB.propertyIdDesired == propertySPX
        txTradeB.amountDesired == 5.indivisible
        txTradeB.amountRemaining == 0.05.divisible
        txTradeB.amountToFill == 1.indivisible
        txTradeB.matches.size() == 1
        if (omniGetInfo().omnicoreversion_int < 1100000) {
            assert txTradeB.unitPrice == "0.11000000000000000000000000000000000000000000000000"
        } else {
            assert txTradeB.unitPrice == "9.09090909090909090909090909090909090909090909090909"
        }

        when:
        def tradeMatchB = txTradeB.matches.find { it.txId == txidTradeA }

        then:
        tradeMatchB.address == actorA
        tradeMatchB.amountSold == 0.5.divisible
        tradeMatchB.amountReceived == 5.indivisible
    }

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/38
     */
    def "Orders fill with the maximal amount at the best price possible"() {
        def actorA = createFundedAddress(startBTC, 0.00000020.divisible, false)
        def actorB = createFundedAddress(startBTC, zeroAmount, false)
        def propertyOMNI = CurrencyID.TOMNI
        def propertySPX = fundNewProperty(actorB, 12.divisible, propertyOMNI.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertyOMNI, 0.00000020.divisible , propertySPX, 10.indivisible)
        generateBlocks(1)
        def txidTradeB = omniSendTrade(actorB, propertySPX, 12.indivisible, propertyOMNI, 0.00000017.divisible)
        generateBlocks(1)

        then:
        omniGetTrade(txidTradeA).valid
        omniGetTrade(txidTradeB).valid

        and:
        omniGetTrade(txidTradeA).status == "filled"
        omniGetTrade(txidTradeB).status != "open"
        omniGetTrade(txidTradeB).status != "filled"

        and:
        omniGetBalance(actorA, propertySPX).balance == 10 as BigDecimal
        omniGetBalance(actorA, propertyOMNI).reserved == 0.0
        omniGetBalance(actorB, propertyOMNI).balance == 0.00000020
        omniGetBalance(actorB, propertySPX).reserved == 2 as BigDecimal
    }

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/41
     */
    def "Orders with inverted price and different amounts for sale match"() {
        def actorA = createFundedAddress(startBTC, zeroAmount, false)
        def actorB = createFundedAddress(startBTC, 1.66666666.divisible, false)
        def propertyOMNI = CurrencyID.OMNI
        def propertySPX = fundNewProperty(actorA, 1.divisible, propertyOMNI.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertySPX, 1.divisible, propertyOMNI, 2.divisible)
        generateBlocks(1)
        def txidTradeB = omniSendTrade(actorB, propertyOMNI, 1.66666666.divisible, propertySPX, 0.83333333.divisible)
        generateBlocks(1)

        then:
        omniGetTrade(txidTradeA).valid
        omniGetTrade(txidTradeB).valid

        and:
        omniGetTrade(txidTradeA).status != "open"
        omniGetTrade(txidTradeA).status != "filled"
        omniGetTrade(txidTradeB).status == "filled"

        and:
        omniGetBalance(actorA, propertyOMNI).balance == 1.66666666
        omniGetBalance(actorA, propertySPX).reserved == 0.16666667
        omniGetBalance(actorB, propertySPX).balance == 0.83333333
        omniGetBalance(actorB, propertyOMNI).reserved == 0.0
    }

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/40
     */
    def "Intermediate results are not truncated or rounded"() {
        def actorA = createFundedAddress(startBTC, 23.divisible, false)
        def actorB = createFundedAddress(startBTC, zeroAmount, false)
        def propertyOMNI = CurrencyID.TOMNI
        def propertySPX = fundNewProperty(actorB, 50.divisible, propertyOMNI.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertyOMNI, 23.divisible , propertySPX, 100.divisible)
        generateBlocks(1)
        def txidTradeB = omniSendTrade(actorB, propertySPX, 50.divisible, propertyOMNI, 10.divisible)
        generateBlocks(1)

        then:
        omniGetTrade(txidTradeA).valid
        omniGetTrade(txidTradeB).valid

        and:
        omniGetTrade(txidTradeA).status != "open"
        omniGetTrade(txidTradeA).status != "filled"
        omniGetTrade(txidTradeB).status == "filled"

        and:
        omniGetBalance(actorA, propertySPX).balance == 50.0
        omniGetBalance(actorA, propertyOMNI).reserved == 11.5
        omniGetBalance(actorB, propertyOMNI).balance == 11.5
        omniGetBalance(actorB, propertySPX).reserved == 0.0
    }

    def "One side of the trade must either be OMNI or TOMNI"() {
        def actorAdress = createFundedAddress(startBTC, zeroAmount, false)
        def propertySPA = fundNewProperty(actorAdress, 20.divisible, Ecosystem.TOMNI)
        def propertySPB = fundNewProperty(actorAdress, 10.divisible, Ecosystem.TOMNI)

        when:
        def txidTrade = omniSendTrade(actorAdress, propertySPA, 1.divisible, propertySPB, 1.divisible)
        generateBlocks(1)

        then:
        !omniGetTrade(txidTrade).valid

        and:
        omniGetBalance(actorAdress, propertySPA).balance == 20.0
        omniGetBalance(actorAdress, propertySPB).balance == 10.0
        omniGetBalance(actorAdress, propertySPA).reserved == 0.0
        omniGetBalance(actorAdress, propertySPB).reserved == 0.0
    }

    @Unroll
    def "Exact trade match: #amountOMNI OMNI for #amountSPX SPX"() {
        when:
        def traderA = createFundedAddress(startBTC, amountOMNI, false)  // offers OMNI
        def traderB = createFundedAddress(startBTC, zeroAmount, false) // offers SPX
        def propertySPX = fundNewProperty(traderB, amountSPX, propertyOMNI.ecosystem)

        then:
        omniGetBalance(traderA, propertyOMNI).balance == amountOMNI.numberValue()
        omniGetBalance(traderA, propertySPX).balance == 0.0
        omniGetBalance(traderB, propertyOMNI).balance == 0.0
        omniGetBalance(traderB, propertySPX).balance == amountSPX.numberValue()

        when: "trader A offers OMNI and desired SPX"
        def txidOfferA = omniSendTrade(traderA, propertyOMNI, amountOMNI, propertySPX, amountSPX)
        generateBlocks(1)

        then: "it is a valid open order"
        omniGetTrade(txidOfferA).valid
        omniGetTrade(txidOfferA).status == "open"
        omniGetTrade(txidOfferA).propertyIdForSale == propertyOMNI
        omniGetTrade(txidOfferA).propertyIdDesired == propertySPX

        and: "there is an offering for the new property in the orderbook"
        omniGetOrderbook(propertyOMNI, propertySPX).size() == 1

        and: "the offered amount is now reserved"
        omniGetBalance(traderA, propertyOMNI).balance == zeroAmount.numberValue()
        omniGetBalance(traderA, propertyOMNI).reserved == amountOMNI.numberValue()

        when: "trader B offers SPX and desires OMNI"
        def txidOfferB = omniSendTrade(traderB, propertySPX, amountSPX, propertyOMNI, amountOMNI)
        generateBlocks(1)

        then: "the order is filled"
        omniGetTrade(txidOfferB).valid
        omniGetTrade(txidOfferB).status == "filled"
        omniGetTrade(txidOfferB).propertyIdForSale == propertySPX
        omniGetTrade(txidOfferB).propertyIdDesired == propertyOMNI

        and: "the offering is no longer listed in the orderbook"
        omniGetOrderbook(propertyOMNI, propertySPX).size() == 0

        and:
        omniGetBalance(traderA, propertyOMNI).balance == 0.0
        omniGetBalance(traderA, propertySPX).balance == amountSPX.numberValue()
        omniGetBalance(traderB, propertyOMNI).balance == amountOMNI.numberValue()
        omniGetBalance(traderB, propertySPX).balance == 0.0

        and:
        omniGetBalance(traderA, propertyOMNI).reserved == 0.0
        omniGetBalance(traderA, propertySPX).reserved == 0.0
        omniGetBalance(traderB, propertyOMNI).reserved == 0.0
        omniGetBalance(traderB, propertySPX).reserved == 0.0

        where:
        amountSPX                       | propertyOMNI     | amountOMNI
        3.0.divisible                   | CurrencyID.OMNI  | 6.0.divisible
        2.0.divisible                   | CurrencyID.TOMNI | 0.6.divisible
        50.0.divisible                  | CurrencyID.OMNI  | 0.1.divisible
        0.99999999.divisible            | CurrencyID.TOMNI | 0.11111111.divisible
        3333.33333333.divisible         | CurrencyID.OMNI  | 0.0125.divisible
        0.03.divisible                  | CurrencyID.TOMNI | 0.009.divisible
        0.00000001.divisible            | CurrencyID.OMNI  | 0.00000003.divisible
        10000000001.0.divisible         | CurrencyID.TOMNI | 0.00000002.divisible
        92233720368.54775807.divisible  | CurrencyID.OMNI  | 0.00000001.divisible
        7.indivisible                   | CurrencyID.TOMNI | 0.33333333.divisible
        1.indivisible                   | CurrencyID.OMNI  | 0.00000001.divisible
        33333.indivisible               | CurrencyID.TOMNI | 0.0001.divisible
        4815162342.indivisible          | CurrencyID.OMNI  | 0.00101011.divisible
        1000000000000000051.indivisible | CurrencyID.TOMNI | 0.00000001.divisible
        9223372036854775807.indivisible | CurrencyID.OMNI  | 0.00000006.divisible
    }

    @Unroll
    def "Exact trade match: #amountSPX SPX for #amountOMNI OMNI"() {
        when:
        def traderA = createFundedAddress(startBTC, zeroAmount, false) // offers SPX
        def traderB = createFundedAddress(startBTC, amountOMNI, false)  // offers OMNI
        def propertySPX = fundNewProperty(traderA, amountSPX, propertyOMNI.ecosystem)

        then:
        omniGetBalance(traderA, propertyOMNI).balance == 0.0
        omniGetBalance(traderA, propertySPX).balance == amountSPX.numberValue()
        omniGetBalance(traderB, propertyOMNI).balance == amountOMNI.numberValue()
        omniGetBalance(traderB, propertySPX).balance == 0.0

        when: "trader A offers SPX and desired OMNI"
        def txidOfferA = omniSendTrade(traderA, propertySPX, amountSPX, propertyOMNI, amountOMNI)
        generateBlocks(1)

        then: "it is a valid open order"
        omniGetTrade(txidOfferA).valid
        omniGetTrade(txidOfferA).status == "open"
        omniGetTrade(txidOfferA).propertyIdForSale == propertySPX
        omniGetTrade(txidOfferA).propertyIdDesired == propertyOMNI

        and: "there is an offering for the new property in the orderbook"
        omniGetOrderbook(propertySPX, propertyOMNI).size() == 1

        and: "the offered amount is now reserved"
        omniGetBalance(traderA, propertySPX).balance == 0.0
        omniGetBalance(traderA, propertySPX).reserved == amountSPX.numberValue()

        when: "trader B offers OMNI and desires SPX"
        def txidOfferB = omniSendTrade(traderB, propertyOMNI, amountOMNI, propertySPX, amountSPX)
        generateBlocks(1)

        then: "the order is filled"
        omniGetTrade(txidOfferB).valid
        omniGetTrade(txidOfferB).status == "filled"
        omniGetTrade(txidOfferB).propertyIdForSale == propertyOMNI
        omniGetTrade(txidOfferB).propertyIdDesired == propertySPX

        and: "the offering is no longer listed in the orderbook"
        omniGetOrderbook(propertySPX, propertyOMNI).size() == 0

        and:
        omniGetBalance(traderA, propertyOMNI).balance == amountOMNI.numberValue()
        omniGetBalance(traderA, propertySPX).balance == 0.0
        omniGetBalance(traderB, propertyOMNI).balance == 0.0
        omniGetBalance(traderB, propertySPX).balance == amountSPX.numberValue()

        and:
        omniGetBalance(traderA, propertyOMNI).reserved == 0.0
        omniGetBalance(traderA, propertySPX).reserved == 0.0
        omniGetBalance(traderB, propertyOMNI).reserved == 0.0
        omniGetBalance(traderB, propertySPX).reserved == 0.0

        where:
        amountSPX                       | propertyOMNI    | amountOMNI
        101.indivisible                 | CurrencyID.OMNI | 0.1.divisible
        100.02.divisible                | CurrencyID.OMNI | 0.01.divisible
        1000003.indivisible             | CurrencyID.OMNI | 0.001.divisible
        10000.0004.divisible            | CurrencyID.OMNI | 0.0001.divisible
        100000000005.indivisible        | CurrencyID.OMNI | 0.00001.divisible
        1000000.0000006.divisible       | CurrencyID.OMNI | 0.000001.divisible
        1000000000000007.indivisible    | CurrencyID.OMNI | 0.0000001.divisible
        100000000.00000008.divisible    | CurrencyID.TOMNI | 0.00000007.divisible
        444444444444444449.indivisible  | CurrencyID.TOMNI | 0.00000051.divisible
        92233720368.54775807.divisible  | CurrencyID.TOMNI | 0.00000001.divisible
        353535.indivisible              | CurrencyID.TOMNI | 0.07171717.divisible
        0.0000513.divisible             | CurrencyID.TOMNI | 0.00005511.divisible
        100.indivisible                 | CurrencyID.TOMNI | 0.00002222.divisible
        0.77777776.divisible            | CurrencyID.TOMNI | 0.00000333.divisible
        9223372036854775807.indivisible | CurrencyID.TOMNI | 0.00000021.divisible
    }

    def setupSpec() {
        if (!commandExists("omni_gettrade")) {
            Assumptions.abort('The client has no "omni_gettrade" command')
        }
        if (!commandExists("omni_getorderbook")) {
            Assumptions.abort('The client has no "omni_getorderbook" command')
        }
    }

}
