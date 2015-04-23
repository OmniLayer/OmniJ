package foundation.omni.test.rpc.reorgs

import com.msgilligan.bitcoin.rpc.JsonRPCStatusException
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import foundation.omni.rpc.SmartPropertyListInfo
import spock.lang.Shared
import spock.lang.Unroll

class PropertyCreationReorgSpec extends BaseReorgSpec {

    @Shared
    List<SmartPropertyListInfo> propertyListAtStart

    @Shared
    CurrencyID nextMainPropertyID

    @Shared
    CurrencyID nextTestPropertyID

    def setupSpec() {
        propertyListAtStart = listproperties_MP()
        def mainProperties = propertyListAtStart.findAll { it.id.ecosystem == Ecosystem.MSC }
        def testProperties = propertyListAtStart.findAll { it.id.ecosystem == Ecosystem.TMSC }
        def lastMainPropertyID = mainProperties.last().id
        def lastTestPropertyID = testProperties.last().id

        if (lastMainPropertyID == CurrencyID.MSC) {
            nextMainPropertyID = new CurrencyID(CurrencyID.TMSC_VALUE + 1)
        } else {
            nextMainPropertyID = new CurrencyID(lastMainPropertyID.longValue() + 1)
        }

        if (lastTestPropertyID == CurrencyID.TMSC) {
            nextTestPropertyID = new CurrencyID(2147483651L)
        } else {
            nextTestPropertyID = new CurrencyID(lastTestPropertyID.longValue() + 1)
        }
    }

    @Unroll
    def "In #ecosystem, after invalidating the creation of #propertyType, the transaction and the property are invalid"()
    {
        def actorAddress = createFundedAddress(startBTC, startMSC)

        when: "broadcasting and confirming a property creation transaction"
        def txid = createProperty(actorAddress, ecosystem, propertyType, numberOfTokens.longValue())
        def blockHashOfCreation = generateAndGetBlockHash()

        then: "the transaction is valid"
        checkTransactionValidity(txid)

        and: "a new property was created"
        listproperties_MP().size() == propertyListAtStart.size() + 1

        and: "the created property is in the correct ecosystem"
        def txCreation = getTransactionMP(txid)
        def currencyID = new CurrencyID(txCreation.propertyid as long)
        currencyID.ecosystem == ecosystem

        and: "it has the expected next currency identifier"
        currencyID == expectedCurrencyID

        and: "the creator was credited with the correct amount of created tokens"
        getbalance_MP(actorAddress, currencyID).balance == expectedBalance

        when: "invalidating the block and property creation transaction"
        invalidateBlock(blockHashOfCreation)
        clearMemPool()
        generateBlock()

        then: "the transaction is no longer valid"
        !checkTransactionValidity(txid)

        and: "the created property is no longer listed"
        listproperties_MP().size() == propertyListAtStart.size()

        when:
        getproperty_MP(currencyID)

        then: "no information about the property is available"
        thrown(JsonRPCStatusException)

        when:
        getbalance_MP(actorAddress, currencyID)

        then: "no balance information for the property"
        thrown(JsonRPCStatusException)

        where:
        ecosystem      | propertyType             | numberOfTokens        | expectedBalance               | expectedCurrencyID
        Ecosystem.MSC  | PropertyType.INDIVISIBLE | new Long("150")       | new BigDecimal("150")         | nextMainPropertyID
        Ecosystem.MSC  | PropertyType.DIVISIBLE   | new Long("100000000") | new BigDecimal("1.00000000")  | nextMainPropertyID
        Ecosystem.TMSC | PropertyType.INDIVISIBLE | new Long("350")       | new BigDecimal("350")         | nextTestPropertyID
        Ecosystem.TMSC | PropertyType.DIVISIBLE   | new Long("250000000") | new BigDecimal("2.50000000")  | nextTestPropertyID
    }

}
