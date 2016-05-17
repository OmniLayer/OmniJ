package foundation.omni.test.rpc.reorgs

import com.msgilligan.bitcoinj.rpc.JsonRPCStatusException
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.rpc.SmartPropertyListInfo
import org.bitcoinj.core.Coin
import spock.lang.Shared
import spock.lang.Unroll

/**
 * To confirm that the database for smart properties/tokens remains
 * consistent during reorganizations, new tokens are created and the total
 * number of created tokens, and their identifiers, are checked against
 * expected values.
 *
 * The tests cover the case where snapshots of the state are restored from
 * persisted data, but also consider the situation where no snapshot is
 * available, and the whole state is wiped and reconstructed.
 */
class PropertyCreationReorgSpec extends BaseReorgSpec {

    @Shared
    List<SmartPropertyListInfo> propertyListAtStart

    @Shared
    CurrencyID nextMainPropertyID

    @Shared
    CurrencyID nextTestPropertyID

    def setupSpec() {
        // two extra tokens are created to ensure there are
        // tokens, other than the hardcoded base tokens
        def dummyA = createFundedAddress(Coin.CENT, 0.divisible, false)
        def dummyB = createFundedAddress(Coin.CENT, 0.divisible, false)
        fundNewProperty(dummyA, 3405691582.indivisible, Ecosystem.OMNI)
        fundNewProperty(dummyB, 4276994270.indivisible, Ecosystem.TOMNI)
        generateBlock()

        propertyListAtStart = omniListProperties()
        def mainProperties = propertyListAtStart.findAll { it.propertyid.ecosystem == Ecosystem.OMNI }
        def testProperties = propertyListAtStart.findAll { it.propertyid.ecosystem == Ecosystem.TOMNI }
        def lastMainPropertyID = mainProperties.last().propertyid
        def lastTestPropertyID = testProperties.last().propertyid

        if (lastMainPropertyID == CurrencyID.OMNI) {
            nextMainPropertyID = new CurrencyID(CurrencyID.TOMNI_VALUE + 1)
        } else {
            nextMainPropertyID = new CurrencyID(lastMainPropertyID.getValue() + 1)
        }

        if (lastTestPropertyID == CurrencyID.TOMNI) {
            nextTestPropertyID = new CurrencyID(2147483651L)
        } else {
            nextTestPropertyID = new CurrencyID(lastTestPropertyID.getValue() + 1)
        }
    }

    @Unroll
    def "In #ecosystem, after invalidating the creation of #value tokens, the transaction and the tokens are invalid"()
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

        when: "invalidating the block and property creation transaction after zero or more blocks"
        for (int i = 0; i < extraBlocks; ++i) {
            // after a certain number of blocks the whole state is cleared,
            // initiating the reprocessing of all Omni transactions
            generateBlock()
        }
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
        ecosystem       | value           | extraBlocks | expectedCurrencyID
        Ecosystem.OMNI  | 150.indivisible | 51          | nextMainPropertyID
        Ecosystem.OMNI  | 1.0.divisible   | 3           | nextMainPropertyID
        Ecosystem.TOMNI | 350.indivisible | 53          | nextTestPropertyID
        Ecosystem.TOMNI | 2.5.divisible   | 0           | nextTestPropertyID
    }

}
