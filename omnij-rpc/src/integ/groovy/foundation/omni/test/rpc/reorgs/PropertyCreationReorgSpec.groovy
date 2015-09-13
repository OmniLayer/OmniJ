package foundation.omni.test.rpc.reorgs

import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.OmniValue
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
        propertyListAtStart = omniListProperties()
        def mainProperties = propertyListAtStart.findAll { it.id.ecosystem == Ecosystem.MSC }
        def testProperties = propertyListAtStart.findAll { it.id.ecosystem == Ecosystem.TMSC }
        def lastMainPropertyID = mainProperties.last().id
        def lastTestPropertyID = testProperties.last().id

        if (lastMainPropertyID == CurrencyID.MSC) {
            nextMainPropertyID = new CurrencyID(CurrencyID.TMSC_VALUE + 1)
        } else {
            nextMainPropertyID = new CurrencyID(lastMainPropertyID.getValue() + 1)
        }

        if (lastTestPropertyID == CurrencyID.TMSC) {
            nextTestPropertyID = new CurrencyID(2147483651L)
        } else {
            nextTestPropertyID = new CurrencyID(lastTestPropertyID.getValue() + 1)
        }
    }

    @Unroll
    def "In #ecosystem, after invalidating the creation of #propertyType, the transaction and the property are invalid"()
    {
        def actorAddress = createFundedAddress(startBTC, startMSC)

        when: "broadcasting and confirming a property creation transaction"
        def txid = createProperty(actorAddress, ecosystem, value)
        def blockHashOfCreation = generateAndGetBlockHash()

        then: "the transaction is valid"
        checkTransactionValidity(txid)

        and: "a new property was created"
        omniListProperties().size() == propertyListAtStart.size() + 1

        and: "the created property is in the correct ecosystem"
        def txCreation = omniGetTransaction(txid)
        def currencyID = new CurrencyID(txCreation.propertyid as long)
        currencyID.ecosystem == ecosystem

        and: "it has the expected next currency identifier"
        currencyID == expectedCurrencyID

        and: "the creator was credited with the correct amount of created tokens"
        omniGetBalance(actorAddress, currencyID).balance == value.bigDecimalValue()

        when: "invalidating the block and property creation transaction"
        invalidateBlock(blockHashOfCreation)
        clearMemPool()
        generateBlock()

        then: "the transaction is no longer valid"
        !checkTransactionValidity(txid)

        and: "the created property is no longer listed"
        omniListProperties().size() == propertyListAtStart.size()

        when:
        omniGetProperty(currencyID)

        then: "no information about the property is available"
        thrown(JsonRPCStatusException)

        when:
        omniGetBalance(actorAddress, currencyID)

        then: "no balance information for the property"
        thrown(JsonRPCStatusException)

        where:
        ecosystem      | value              | expectedCurrencyID
        Ecosystem.MSC  | 150.indivisible    | nextMainPropertyID
        Ecosystem.MSC  | 1.0.divisible      | nextMainPropertyID
        Ecosystem.TMSC | 350.indivisible    | nextTestPropertyID
        Ecosystem.TMSC | 2.5.divisible      | nextTestPropertyID
    }

}
