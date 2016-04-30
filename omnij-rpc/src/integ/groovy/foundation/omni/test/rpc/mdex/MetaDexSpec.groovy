package foundation.omni.test.rpc.mdex

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniValue
import foundation.omni.PropertyType
import org.bitcoinj.core.Coin
import org.junit.internal.AssumptionViolatedException
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
        def propertyMSC = CurrencyID.MSC
        def propertySPX = fundNewProperty(actorB, 10.divisible, propertyMSC.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertyMSC, 0.00000006.divisible , propertySPX, 6.indivisible)
        generateBlock()
        def txidTradeB = omniSendTrade(actorB, propertySPX, 10.indivisible, propertyMSC, 0.00000001.divisible)
        generateBlock()
        def txidTradeC = omniSendTrade(actorC, propertyMSC, 0.00000001.divisible, propertySPX, 10.indivisible)
        generateBlock()
        def txidTradeD = omniSendTrade(actorD, propertyMSC, 0.00000001.divisible, propertySPX, 4.indivisible)
        generateBlock()

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
        omniGetOrderbook(propertyMSC, propertySPX).size() == 1
        omniGetOrderbook(propertySPX, propertyMSC).size() == 0

        and:
        omniGetBalance(actorC, propertyMSC).reserved == 0.00000001

        when: "retrieving additional information about trade C"
        def txTradeC = omniGetTrade(txidTradeC)

        then: "the order is still unmatched"
        txTradeC.txid == txidTradeC.toString()
        txTradeC.sendingaddress == actorC.toString()
        txTradeC.version == 0
        txTradeC.type_int == 25
        txTradeC.type == "MetaDEx trade"
        txTradeC.propertyidforsale == propertyMSC.getValue()
        txTradeC.propertyidforsaleisdivisible
        txTradeC.amountforsale as BigDecimal == 0.00000001.divisible.bigDecimalValue()
        txTradeC.propertyiddesired == propertySPX.getValue()
        txTradeC.amountdesired as BigDecimal == 10.indivisible.bigDecimalValue()
        txTradeC.unitprice == "1000000000.00000000000000000000000000000000000000000000000000"
        txTradeC.amountremaining as BigDecimal == 0.00000001.divisible.bigDecimalValue()
        txTradeC.amounttofill as BigDecimal == 10.indivisible.bigDecimalValue()
        txTradeC.matches.size == 0
    }

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/37
     */
    def "Two orders can match, and both can be partially filled"() {
        def actorA = createFundedAddress(startBTC, zeroAmount, false)
        def actorB = createFundedAddress(startBTC, 0.55.divisible, false)
        def propertyMSC = CurrencyID.MSC
        def propertySPX = fundNewProperty(actorA, 25.indivisible, propertyMSC.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertySPX, 25.indivisible, propertyMSC, 2.50.divisible)
        generateBlock()
        def txidTradeB = omniSendTrade(actorB, propertyMSC, 0.55.divisible, propertySPX, 5.indivisible)
        generateBlock()

        then:
        omniGetTransaction(txidTradeA).valid
        omniGetTransaction(txidTradeB).valid

        and:
        omniGetBalance(actorA, propertyMSC).balance == 0.5
        omniGetBalance(actorA, propertySPX).reserved == 20 as BigDecimal
        omniGetBalance(actorB, propertySPX).balance == 5 as BigDecimal
        omniGetBalance(actorB, propertyMSC).reserved == 0.05

        when: "retrieving information about trade A"
        def txTradeA = omniGetTrade(txidTradeA)

        then:
        txTradeA.status == "open part filled"

        and:
        txTradeA.propertyidforsale == propertySPX.getValue()
        txTradeA.amountforsale as BigDecimal == 25.indivisible.bigDecimalValue()
        txTradeA.propertyiddesired == propertyMSC.getValue()
        txTradeA.amountdesired as BigDecimal == 2.5.divisible.bigDecimalValue()
        txTradeA.unitprice == "0.10000000000000000000000000000000000000000000000000"
        txTradeA.amountremaining as BigDecimal == 20.indivisible.bigDecimalValue()
        txTradeA.amounttofill as BigDecimal == 2.0.divisible.bigDecimalValue()
        txTradeA.matches.size == 1

        when:
        def tradeMatchA = txTradeA.matches.find { it.txid == txidTradeB.toString() } as Map<String, Object>

        then:
        tradeMatchA.address == actorB.toString()
        tradeMatchA.amountsold as BigDecimal == 5.divisible.bigDecimalValue()
        tradeMatchA.amountreceived as BigDecimal == 0.5.divisible.bigDecimalValue()

        when: "retrieving information about trade B"
        def txTradeB = omniGetTrade(txidTradeB)

        then:
        txTradeB.status == "open part filled"

        and:
        txTradeB.propertyidforsale == propertyMSC.getValue()
        txTradeB.amountforsale as BigDecimal == 0.55.divisible.bigDecimalValue()
        txTradeB.propertyiddesired == propertySPX.getValue()
        txTradeB.amountdesired as BigDecimal == 5.indivisible.bigDecimalValue()
        txTradeB.unitprice == "9.09090909090909090909090909090909090909090909090909"
        txTradeB.amountremaining as BigDecimal == 0.05.divisible.bigDecimalValue()
        txTradeB.amounttofill as BigDecimal == 1.indivisible.bigDecimalValue()
        txTradeB.matches.size == 1

        when:
        def tradeMatchB = txTradeB.matches.find { it.txid == txidTradeA.toString() } as Map<String, Object>

        then:
        tradeMatchB.address == actorA.toString()
        tradeMatchB.amountsold as BigDecimal == 0.5.divisible.bigDecimalValue()
        tradeMatchB.amountreceived as BigDecimal == 5.indivisible.bigDecimalValue()
    }

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/38
     */
    def "Orders fill with the maximal amount at the best price possible"() {
        def actorA = createFundedAddress(startBTC, 0.00000020.divisible, false)
        def actorB = createFundedAddress(startBTC, zeroAmount, false)
        def propertyMSC = CurrencyID.TMSC
        def propertySPX = fundNewProperty(actorB, 12.divisible, propertyMSC.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertyMSC, 0.00000020.divisible , propertySPX, 10.indivisible)
        generateBlock()
        def txidTradeB = omniSendTrade(actorB, propertySPX, 12.indivisible, propertyMSC, 0.00000017.divisible)
        generateBlock()

        then:
        omniGetTrade(txidTradeA).valid
        omniGetTrade(txidTradeB).valid

        and:
        omniGetTrade(txidTradeA).status == "filled"
        omniGetTrade(txidTradeB).status != "open"
        omniGetTrade(txidTradeB).status != "filled"

        and:
        omniGetBalance(actorA, propertySPX).balance == 10 as BigDecimal
        omniGetBalance(actorA, propertyMSC).reserved == 0.0
        omniGetBalance(actorB, propertyMSC).balance == 0.00000020
        omniGetBalance(actorB, propertySPX).reserved == 2 as BigDecimal
    }

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/41
     */
    def "Orders with inverted price and different amounts for sale match"() {
        def actorA = createFundedAddress(startBTC, zeroAmount, false)
        def actorB = createFundedAddress(startBTC, 1.66666666.divisible, false)
        def propertyMSC = CurrencyID.MSC
        def propertySPX = fundNewProperty(actorA, 1.divisible, propertyMSC.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertySPX, 1.divisible, propertyMSC, 2.divisible)
        generateBlock()
        def txidTradeB = omniSendTrade(actorB, propertyMSC, 1.66666666.divisible, propertySPX, 0.83333333.divisible)
        generateBlock()

        then:
        omniGetTrade(txidTradeA).valid
        omniGetTrade(txidTradeB).valid

        and:
        omniGetTrade(txidTradeA).status != "open"
        omniGetTrade(txidTradeA).status != "filled"
        omniGetTrade(txidTradeB).status == "filled"

        and:
        omniGetBalance(actorA, propertyMSC).balance == 1.66666666
        omniGetBalance(actorA, propertySPX).reserved == 0.16666667
        omniGetBalance(actorB, propertySPX).balance == 0.83333333
        omniGetBalance(actorB, propertyMSC).reserved == 0.0
    }

    /**
     * @see: https://github.com/OmniLayer/omnicore/issues/40
     */
    def "Intermediate results are not truncated or rounded"() {
        def actorA = createFundedAddress(startBTC, 23.divisible, false)
        def actorB = createFundedAddress(startBTC, zeroAmount, false)
        def propertyMSC = CurrencyID.TMSC
        def propertySPX = fundNewProperty(actorB, 50.divisible, propertyMSC.ecosystem)

        when:
        def txidTradeA = omniSendTrade(actorA, propertyMSC, 23.divisible , propertySPX, 100.divisible)
        generateBlock()
        def txidTradeB = omniSendTrade(actorB, propertySPX, 50.divisible, propertyMSC, 10.divisible)
        generateBlock()

        then:
        omniGetTrade(txidTradeA).valid
        omniGetTrade(txidTradeB).valid

        and:
        omniGetTrade(txidTradeA).status != "open"
        omniGetTrade(txidTradeA).status != "filled"
        omniGetTrade(txidTradeB).status == "filled"

        and:
        omniGetBalance(actorA, propertySPX).balance == 50.0
        omniGetBalance(actorA, propertyMSC).reserved == 11.5
        omniGetBalance(actorB, propertyMSC).balance == 11.5
        omniGetBalance(actorB, propertySPX).reserved == 0.0
    }

    def "One side of the trade must either be MSC or TMSC"() {
        def actorAdress = createFundedAddress(startBTC, zeroAmount, false)
        def propertySPA = fundNewProperty(actorAdress, 20.divisible, Ecosystem.TMSC)
        def propertySPB = fundNewProperty(actorAdress, 10.divisible, Ecosystem.TMSC)

        when:
        def txidTrade = omniSendTrade(actorAdress, propertySPA, 1.divisible, propertySPB, 1.divisible)
        generateBlock()

        then:
        omniGetTrade(txidTrade).valid == false

        and:
        omniGetBalance(actorAdress, propertySPA).balance == 20.0
        omniGetBalance(actorAdress, propertySPB).balance == 10.0
        omniGetBalance(actorAdress, propertySPA).reserved == 0.0
        omniGetBalance(actorAdress, propertySPB).reserved == 0.0
    }

    @Unroll
    def "Exact trade match: #amountMSC MSC for #amountSPX SPX"() {
        when:
        def traderA = createFundedAddress(startBTC, amountMSC, false)  // offers MSC
        def traderB = createFundedAddress(startBTC, zeroAmount, false) // offers SPX
        def propertySPX = fundNewProperty(traderB, amountSPX, propertyMSC.ecosystem)

        then:
        omniGetBalance(traderA, propertyMSC).balance == amountMSC.numberValue()
        omniGetBalance(traderA, propertySPX).balance == 0.0
        omniGetBalance(traderB, propertyMSC).balance == 0.0
        omniGetBalance(traderB, propertySPX).balance == amountSPX.numberValue()

        when: "trader A offers MSC and desired SPX"
        def txidOfferA = omniSendTrade(traderA, propertyMSC, amountMSC, propertySPX, amountSPX)
        generateBlock()

        then: "it is a valid open order"
        omniGetTrade(txidOfferA).valid
        omniGetTrade(txidOfferA).status == "open"
        omniGetTrade(txidOfferA).propertyidforsale == propertyMSC.getValue()
        omniGetTrade(txidOfferA).propertyiddesired == propertySPX.getValue()

        and: "there is an offering for the new property in the orderbook"
        omniGetOrderbook(propertyMSC, propertySPX).size() == 1

        and: "the offered amount is now reserved"
        omniGetBalance(traderA, propertyMSC).balance == zeroAmount.numberValue()
        omniGetBalance(traderA, propertyMSC).reserved == amountMSC.numberValue()

        when: "trader B offers SPX and desires MSC"
        def txidOfferB = omniSendTrade(traderB, propertySPX, amountSPX, propertyMSC, amountMSC)
        generateBlock()

        then: "the order is filled"
        omniGetTrade(txidOfferB).valid
        omniGetTrade(txidOfferB).status == "filled"
        omniGetTrade(txidOfferB).propertyidforsale == propertySPX.getValue()
        omniGetTrade(txidOfferB).propertyiddesired == propertyMSC.getValue()

        and: "the offering is no longer listed in the orderbook"
        omniGetOrderbook(propertyMSC, propertySPX).size() == 0

        and:
        omniGetBalance(traderA, propertyMSC).balance == 0.0
        omniGetBalance(traderA, propertySPX).balance == amountSPX.numberValue()
        omniGetBalance(traderB, propertyMSC).balance == amountMSC.numberValue()
        omniGetBalance(traderB, propertySPX).balance == 0.0

        and:
        omniGetBalance(traderA, propertyMSC).reserved == 0.0
        omniGetBalance(traderA, propertySPX).reserved == 0.0
        omniGetBalance(traderB, propertyMSC).reserved == 0.0
        omniGetBalance(traderB, propertySPX).reserved == 0.0

        where:
        amountSPX                       | propertyMSC     | amountMSC
        3.0.divisible                   | CurrencyID.MSC  | 6.0.divisible
        2.0.divisible                   | CurrencyID.TMSC | 0.6.divisible
        50.0.divisible                  | CurrencyID.MSC  | 0.1.divisible
        0.99999999.divisible            | CurrencyID.TMSC | 0.11111111.divisible
        3333.33333333.divisible         | CurrencyID.MSC  | 0.0125.divisible
        0.03.divisible                  | CurrencyID.TMSC | 0.009.divisible
        0.00000001.divisible            | CurrencyID.MSC  | 0.00000003.divisible
        10000000001.0.divisible         | CurrencyID.TMSC | 0.00000002.divisible
        92233720368.54775807.divisible  | CurrencyID.MSC  | 0.00000001.divisible
        7.indivisible                   | CurrencyID.TMSC | 0.33333333.divisible
        1.indivisible                   | CurrencyID.MSC  | 0.00000001.divisible
        33333.indivisible               | CurrencyID.TMSC | 0.0001.divisible
        4815162342.indivisible          | CurrencyID.MSC  | 0.00101011.divisible
        1000000000000000051.indivisible | CurrencyID.TMSC | 0.00000001.divisible
        9223372036854775807.indivisible | CurrencyID.MSC  | 0.00000006.divisible
    }

    @Unroll
    def "Exact trade match: #amountSPX SPX for #amountMSC MSC"() {
        when:
        def traderA = createFundedAddress(startBTC, zeroAmount, false) // offers SPX
        def traderB = createFundedAddress(startBTC, amountMSC, false)  // offers MSC
        def propertySPX = fundNewProperty(traderA, amountSPX, propertyMSC.ecosystem)

        then:
        omniGetBalance(traderA, propertyMSC).balance == 0.0
        omniGetBalance(traderA, propertySPX).balance == amountSPX.numberValue()
        omniGetBalance(traderB, propertyMSC).balance == amountMSC.numberValue()
        omniGetBalance(traderB, propertySPX).balance == 0.0

        when: "trader A offers SPX and desired MSC"
        def txidOfferA = omniSendTrade(traderA, propertySPX, amountSPX, propertyMSC, amountMSC)
        generateBlock()

        then: "it is a valid open order"
        omniGetTrade(txidOfferA).valid
        omniGetTrade(txidOfferA).status == "open"
        omniGetTrade(txidOfferA).propertyidforsale == propertySPX.getValue()
        omniGetTrade(txidOfferA).propertyiddesired == propertyMSC.getValue()

        and: "there is an offering for the new property in the orderbook"
        omniGetOrderbook(propertySPX, propertyMSC).size() == 1

        and: "the offered amount is now reserved"
        omniGetBalance(traderA, propertySPX).balance == 0.0
        omniGetBalance(traderA, propertySPX).reserved == amountSPX.numberValue()

        when: "trader B offers MSC and desires SPX"
        def txidOfferB = omniSendTrade(traderB, propertyMSC, amountMSC, propertySPX, amountSPX)
        generateBlock()

        then: "the order is filled"
        omniGetTrade(txidOfferB).valid
        omniGetTrade(txidOfferB).status == "filled"
        omniGetTrade(txidOfferB).propertyidforsale == propertyMSC.getValue()
        omniGetTrade(txidOfferB).propertyiddesired == propertySPX.getValue()

        and: "the offering is no longer listed in the orderbook"
        omniGetOrderbook(propertySPX, propertyMSC).size() == 0

        and:
        omniGetBalance(traderA, propertyMSC).balance == amountMSC.numberValue()
        omniGetBalance(traderA, propertySPX).balance == 0.0
        omniGetBalance(traderB, propertyMSC).balance == 0.0
        omniGetBalance(traderB, propertySPX).balance == amountSPX.numberValue()

        and:
        omniGetBalance(traderA, propertyMSC).reserved == 0.0
        omniGetBalance(traderA, propertySPX).reserved == 0.0
        omniGetBalance(traderB, propertyMSC).reserved == 0.0
        omniGetBalance(traderB, propertySPX).reserved == 0.0

        where:
        amountSPX                       | propertyMSC     | amountMSC
        101.indivisible                 | CurrencyID.MSC  | 0.1.divisible
        100.02.divisible                | CurrencyID.MSC  | 0.01.divisible
        1000003.indivisible             | CurrencyID.MSC  | 0.001.divisible
        10000.0004.divisible            | CurrencyID.MSC  | 0.0001.divisible
        100000000005.indivisible        | CurrencyID.MSC  | 0.00001.divisible
        1000000.0000006.divisible       | CurrencyID.MSC  | 0.000001.divisible
        1000000000000007.indivisible    | CurrencyID.MSC  | 0.0000001.divisible
        100000000.00000008.divisible    | CurrencyID.TMSC | 0.00000007.divisible
        444444444444444449.indivisible  | CurrencyID.TMSC | 0.00000051.divisible
        92233720368.54775807.divisible  | CurrencyID.TMSC | 0.00000001.divisible
        353535.indivisible              | CurrencyID.TMSC | 0.07171717.divisible
        0.0000513.divisible             | CurrencyID.TMSC | 0.00005511.divisible
        100.indivisible                 | CurrencyID.TMSC | 0.00002222.divisible
        0.77777776.divisible            | CurrencyID.TMSC | 0.00000333.divisible
        9223372036854775807.indivisible | CurrencyID.TMSC | 0.00000021.divisible
    }

    def setupSpec() {
        if (!commandExists("omni_gettrade")) {
            throw new AssumptionViolatedException('The client has no "omni_gettrade" command')
        }
        if (!commandExists("omni_getorderbook")) {
            throw new AssumptionViolatedException('The client has no "omni_getorderbook" command')
        }
    }

}
