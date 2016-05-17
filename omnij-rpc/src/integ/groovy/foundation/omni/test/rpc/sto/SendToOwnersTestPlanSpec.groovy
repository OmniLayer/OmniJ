package foundation.omni.test.rpc.sto

import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniValue
import foundation.omni.dsl.categories.NumberCategory
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Sha256Hash
import com.xlson.groovycsv.CsvParser
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import org.junit.AssumptionViolatedException
import spock.lang.Shared
import spock.lang.Unroll
import spock.util.mop.Use

/**
 * Data driven tests for the "send to owners" transaction type
 */
@Use(NumberCategory)
class SendToOwnersTestPlanSpec extends BaseRegTestSpec {
    final static Coin startBTC = 0.1.btc

    @Shared
    def testdata

    def setupSpec() {
        def file = new File(getTestPlanPath())
        def tsv = file.text
        def data = new CsvParser().parse(tsv, separator: '\t')
        testdata = data
    }

    def getTestPlanPath() {
        // TODO: Remove dependency on current working dir, so tests can be run from IDEA
        return "src/integ/groovy/foundation/omni/test/rpc/sto/sto-testplan.tsv"
    }

    @Unroll
    def "#description"() {
        assert numberOfOwners == sptAvailableOwners.size()
        assert numberOfOwners == sptReservedOwners.size()
        assert numberOfOwners == expectedSPTAvailableOwners.size()
        assert numberOfOwners == expectedSPTReservedOwners.size()

        maybeSkipReservedMetaDexTests(sptReserved, sptReservedOwners)
                                       OmniValue
        given:
        //log.debug "Creating startMSC from: {} + {}", mscAvailable, mscReserved
        def startMSC = OmniDivisibleValue.of(mscAvailable + mscReserved)
        def actorAddress = createFundedAddress(startBTC, startMSC, false)
        def currencyMSC = new CurrencyID(ecosystem.getValue())
        def currencySPT = getStoProperty(actorAddress, data)

        // Create a DEx offer to reserve an amount
        if (mscReserved > 0) {
            reserveAmountMSC(actorAddress, currencyMSC, mscReserved.divisible)
        }

        when: "the owners are funded"
        def owners = [] as List<Address>
        def ownerIds = 0..<numberOfOwners
        ownerIds.each { owners << newAddress }
        owners = owners.sort { it.toString() }
        ownerIds.each { omniSend(actorAddress, owners[it], currencySPT, OmniValue.of(sptAvailableOwners[it], propertyType)) }
        generateBlock()

        then: "the actor starts with the correct #currencySPT and #currencyMSC balance"
        assertBalance(actorAddress, currencyMSC, mscAvailable, mscReserved)
        assertBalance(actorAddress, currencySPT, sptAvailable, sptReserved)

        and: "every owner starts with the correct #currencySPT balance"
        for (id in ownerIds) {
            assertBalance(owners[id], currencySPT, sptAvailableOwners[id], sptReservedOwners[id])
        }

        when: "#amountSTO is sent to owners of #currencySPT"
        def txid = executeSendToOwners(actorAddress, currencySPT, OmniValue.of(amountSTO, propertyType), expectException)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        if (txid != null) {
            def transaction = omniGetTransaction(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        and: "the sender ends up with the expected #currencySPT and #currencyMSC balance"
        assertBalance(actorAddress, currencyMSC, expectedMSCAvailable, expectedMSCReserved)
        assertBalance(actorAddress, currencySPT, expectedSPTAvailable, expectedSPTReserved)

        and: "every owner ends up with the expected #currencySPT balance"
        for (id in ownerIds) {
            log.debug "about to check owner {}: currency: {}, expectedavail: {}, expectedreserved: {}", id, currencySPT, expectedSPTAvailableOwners[id], expectedSPTReservedOwners[id]
            log.debug "class is {}", expectedSPTAvailableOwners[id].class
            Long expectedLong =  expectedSPTAvailableOwners[id]
            BigDecimal expectedBD = (BigDecimal) expectedLong;
            assertBalance(owners[id], currencySPT, (BigDecimal) expectedSPTAvailableOwners[id], (BigDecimal) expectedSPTReservedOwners[id])
        }

        where:
        data << testdata
        description = new String(data.Description)
        ecosystem = new Ecosystem(Short.valueOf(data.Ecosystem))
        propertyType = new PropertyType(Integer.valueOf(data.PropertyType))
        propertyName = new String(data.PropertyName)
        sptAvailable = new BigDecimal(data.AmountAvailable)
        sptReserved = new BigDecimal(data.AmountReserved)
        amountSTO = new BigDecimal(data.AmountSTO)
        mscAvailable = new BigDecimal(data.MSCAvailable)
        mscReserved = new BigDecimal(data.MSCReserved)
        numberOfOwners = new Integer(data.NumOwners)
        sptAvailableOwners = Eval.me(data.AmountAvailableOwners) as List<BigDecimal>
        sptReservedOwners = Eval.me(data.AmountReservedOwners) as List<BigDecimal>
        expectException = new Boolean(data.Exceptional)
        expectedValidity = new Boolean(data.ExpectedValidity)
        expectedSPTAvailable = new BigDecimal(data.ExpectedAmountAvailable)
        expectedSPTReserved = new BigDecimal(data.ExpectedAmountReserved)
        expectedMSCAvailable = new BigDecimal(data.ExpectedMSCAvailable)
        expectedMSCReserved = new BigDecimal(data.ExpectedMSCReserved)
        expectedSPTAvailableOwners = Eval.me(data.ExpectedAmountAvailableOwners) as List<BigDecimal>
        expectedSPTReservedOwners = Eval.me(data.ExpectedAmountReservedOwners) as List<BigDecimal>
    }

    def "STO Property ID is non-existent"() {
        def ecosystem = Ecosystem.TMSC
        def propertyType = PropertyType.DIVISIBLE
        def amountSTO = 1.0
        def startMSC = 2.0.divisible    // test/demo of Omni NumberCategory meta-programming
        def expectException = true
        def expectedValidity = false

        def actorAddress = createFundedAddress(startBTC, startMSC)
        def currencyMSC = new CurrencyID(ecosystem.getValue())
        def currencySPT = new CurrencyID(4294967295L) // does not exist

        given: "the actor starts with #startMSC #currencyMSC"
        assert omniGetBalance(actorAddress, currencyMSC).balance == startMSC.bigDecimalValue()

        when: "#amountSTO is sent to owners of #currencySPT"
        def txid = executeSendToOwners(actorAddress, currencySPT, OmniValue.of(amountSTO, propertyType), expectException)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        if (txid != null) {
            def transaction = omniGetTransaction(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        and: "the sender's balance is still the same"
        omniGetBalance(actorAddress, currencyMSC).balance == startMSC.bigDecimalValue()
    }

    def "STO Property ID is 0 - bitcoin"() {
        def ecosystem = Ecosystem.TMSC
        Coin btcAvailable = 0.001.btc
        Coin btcAvailableOwners = 1.0.btc
        OmniDivisibleValue amountSTO = 0.0001.divisible
        OmniDivisibleValue startMSC = 2.0.divisible
        def expectException = true
        def expectedValidity = false
        def currencyMSC = new CurrencyID(ecosystem.value)

        when: "there is a well funded actor and two owners with bitcoin"
        def actorAddress = createFundedAddress(btcAvailable, startMSC)
        def ownerA = createFundedAddress(btcAvailableOwners, startMSC)
        def ownerB = createFundedAddress(btcAvailableOwners, startMSC)

        then: "they have a certain amount of tokens and coins"
        omniGetBalance(actorAddress, currencyMSC).balance == startMSC.bigDecimalValue()
        getBitcoinBalance(actorAddress) == btcAvailable
        getBitcoinBalance(ownerA) == btcAvailableOwners
        getBitcoinBalance(ownerB) == btcAvailableOwners

        when: "#amountSTO is sent to the bitcoin owners"
        def txid = executeSendToOwners(actorAddress, CurrencyID.BTC, amountSTO, expectException)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        if (txid != null) {
            def transaction = omniGetTransaction(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        and: "the sender paid at worst #stdTxFee * 2 bitcoin for the transaction itself"
        getBitcoinBalance(actorAddress) >= (btcAvailable - (stdTxFee * 2))

        and: "all other balances are still the same"
        omniGetBalance(actorAddress, currencyMSC).balance == startMSC.bigDecimalValue()
        getBitcoinBalance(ownerA) == btcAvailableOwners
        getBitcoinBalance(ownerB) == btcAvailableOwners
    }

    def "Sender owns all the coins of the STO Property, other addresses had non-zero balances but now zero balances"() {
        def ecosystem = Ecosystem.TMSC
        def propertyType = PropertyType.DIVISIBLE
        def amountSTO = OmniDivisibleValue.of(1.0)
        def startMSC = OmniDivisibleValue.of(1.0)
        def expectException = false
        def expectedValidity = false
        def currencyMSC = new CurrencyID(ecosystem.getValue())

        def actorAddress = createFundedAddress(startBTC, startMSC)
        def ownerA = createFundedAddress(startBTC, startMSC)
        def ownerB = createFundedAddress(startBTC, startMSC)

        assert omniGetBalance(actorAddress, currencyMSC).balance == startMSC.numberValue()
        assert omniGetBalance(ownerA, currencyMSC).balance == startMSC.numberValue()
        assert omniGetBalance(ownerB, currencyMSC).balance == startMSC.numberValue()

        // Create property
//        def numberOfTokens = amountSTO
//        if (propertyType == PropertyType.DIVISIBLE) {
//            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
//        }
        def txidCreation = createProperty(ownerA, ecosystem, amountSTO)
        generateBlock()
        def txCreation = omniGetTransaction(txidCreation)
        assert txCreation.valid == true
        assert txCreation.confirmations == 1
        def currencySPT = new CurrencyID(txCreation.propertyid)

        // Owner A has the tokens
        assert omniGetBalance(actorAddress, currencySPT).balance == 0.0
        assert omniGetBalance(ownerA, currencySPT).balance == amountSTO.numberValue()
        assert omniGetBalance(ownerB, currencySPT).balance == 0.0

        // Owner A sends half of the tokens to owner B
        omniSend(ownerA, ownerB, currencySPT, (amountSTO / 2L))
        generateBlock()
        assert omniGetBalance(actorAddress, currencySPT).balance == 0.0
        assert omniGetBalance(ownerA, currencySPT).balance == (amountSTO.numberValue() / 2)
        assert omniGetBalance(ownerB, currencySPT).balance == (amountSTO.numberValue() / 2)

        // Owner A and B send the tokens to the main actor
        omniSend(ownerA, actorAddress, currencySPT, (amountSTO / 2))
        omniSend(ownerB, actorAddress, currencySPT, (amountSTO / 2))
        generateBlock()
        assert omniGetBalance(actorAddress, currencySPT).balance == amountSTO.bigDecimalValue()
        assert omniGetBalance(ownerA, currencySPT).balance == 0.0
        assert omniGetBalance(ownerB, currencySPT).balance == 0.0

        when: "#amountSTO is sent to owners of #currencySPT"
        def txid = executeSendToOwners(actorAddress, currencySPT, amountSTO, expectException)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        if (txid != null) {
            def transaction = omniGetTransaction(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        and: "all balances still the same"
        assert omniGetBalance(actorAddress, currencyMSC).balance == startMSC.numberValue()
        assert omniGetBalance(actorAddress, currencySPT).balance == amountSTO.numberValue()
        assert omniGetBalance(ownerA, currencySPT).balance == 0.0
        assert omniGetBalance(ownerB, currencySPT).balance == 0.0
    }

    def "Owners with similar effective balances, but different available/reserved ratios, receive the same amount"() {
        def ecosystem = Ecosystem.TMSC
//        def propertyType = PropertyType.DIVISIBLE
        def startMSC = 100.divisible
        def amountSTO = 99.divisible
        def reservedOwnerA = 100.divisible
        def reservedOwnerB = 10.divisible
        def reservedOwnerC = 0.divisible
        def expectException = false
        def expectedValidity = true
        def currencyMSC = new CurrencyID(ecosystem.getValue())

        def numberOfAllOwners = omniGetProperty(currencyMSC).size()
        if ((startMSC.willets - amountSTO.willets) < (numberOfAllOwners - 1)) {
            throw new AssumptionViolatedException("actor may not have enough OMNI to pay the fee")
        }

        // fund participants
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def ownerA = createFundedAddress(startBTC, startMSC)
        def ownerB = createFundedAddress(startBTC, startMSC)
        def ownerC = createFundedAddress(startBTC, startMSC)

        // reserve an amount for owner A and B
        reserveAmountMSC(ownerA, currencyMSC, reservedOwnerA)
        reserveAmountMSC(ownerB, currencyMSC, reservedOwnerB)

        // confirm starting balances
        assertBalance(actorAddress, currencyMSC, startMSC.bigDecimalValue(), 0.0)
        assertBalance(ownerA, currencyMSC, (startMSC - reservedOwnerA).bigDecimalValue(), reservedOwnerA.bigDecimalValue())
        assertBalance(ownerB, currencyMSC, (startMSC - reservedOwnerB).bigDecimalValue(), reservedOwnerB.bigDecimalValue())
        assertBalance(ownerC, currencyMSC, (startMSC - reservedOwnerC).bigDecimalValue(), reservedOwnerC.bigDecimalValue())

        when: "#amountSTO is sent to three owners with similar effective balance of #currencyMSC"
        def txid = executeSendToOwners(actorAddress, currencyMSC, amountSTO, expectException)
        generateBlock()

        then: "the transaction is valid"
        if (txid != null) {
            def transaction = omniGetTransaction(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        when: "comparing the updated balances after the send"
        def actorBalance = omniGetBalance(actorAddress, currencyMSC)
        def ownerBalanceA = omniGetBalance(ownerA, currencyMSC)
        def ownerBalanceB = omniGetBalance(ownerB, currencyMSC)
        def ownerBalanceC = omniGetBalance(ownerC, currencyMSC)
        def amountSpentActor = startMSC.bigDecimalValue() - actorBalance.balance
        def amountReceivedOwnerA = ownerBalanceA.balance - (startMSC - reservedOwnerA).bigDecimalValue()
        def amountReceivedOwnerB = ownerBalanceB.balance - (startMSC - reservedOwnerB).bigDecimalValue()
        def amountReceivedOwnerC = ownerBalanceC.balance - (startMSC - reservedOwnerC).bigDecimalValue()

        then: "the actor really spent OMNI and the owners received OMNI"
        amountSpentActor > 0.0
        0.0 < amountReceivedOwnerA
        0.0 < amountReceivedOwnerB
        0.0 < amountReceivedOwnerC

        and: "the reserved balances are unchanged"
        actorBalance.reserved  == 0.0
        ownerBalanceA.reserved == reservedOwnerA.bigDecimalValue()
        ownerBalanceB.reserved == reservedOwnerB.bigDecimalValue()
        ownerBalanceC.reserved == reservedOwnerC.bigDecimalValue()

        and: "the three owners received exactly the same amount"
        amountReceivedOwnerA == amountReceivedOwnerB
        amountReceivedOwnerB == amountReceivedOwnerC
    }

    /**
     * Parses the property identifier and creates a new property, if it's neither OMNI or TOMNI.
     */
    CurrencyID getStoProperty(Address actorAddress, def data) {
        def propertyType = new PropertyType(Integer.valueOf(data.PropertyType))
        def amountAvailableOwners = Eval.me(data.AmountAvailableOwners) as List<BigDecimal>
        def amountAvailable = OmniValue.of(new BigDecimal(data.AmountAvailable), propertyType)
        def ecosystem = new Ecosystem(Short.valueOf(data.Ecosystem))
        def propertyName = new String(data.PropertyName)
        def numberOfTokens = OmniValue.of(0, propertyType)

        if (amountAvailableOwners.size()) {
            numberOfTokens += OmniValue.of((BigDecimal) amountAvailableOwners.sum(), propertyType)
        }

        if (propertyName == "OMNI" || propertyName == "TOMNI") {
            if (numberOfTokens > 0) {
                assert numberOfTokens.propertyType == PropertyType.DIVISIBLE
                requestMSC(actorAddress, (OmniDivisibleValue) numberOfTokens)
                generateBlock()
            }
            return CurrencyID.valueOf(propertyName)
        }

        numberOfTokens += amountAvailable

//        if (propertyType == PropertyType.DIVISIBLE) {
//            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
//        }

        def txid = createProperty(actorAddress, ecosystem, numberOfTokens)
        generateBlock()

        def transaction = omniGetTransaction(txid)
        assert transaction.valid == true
        assert transaction.confirmations == 1

        def currencyID = new CurrencyID(transaction.propertyid)
        return currencyID
    }

    /**
     * Creates an offer on the distributed exchange to reserve an amount.
     */
    def reserveAmountMSC(Address actorAddress, CurrencyID currency, OmniDivisibleValue amount) {
        Coin desiredBTC = 1.0.btc
        Byte blockSpan = 100
        Coin commitFee = 0.0001.btc
        Byte action = 1 // new offer

        def txid = createDexSellOffer(actorAddress, currency, amount, desiredBTC, blockSpan, commitFee, action)
        generateBlock()

        def transaction = omniGetTransaction(txid)
        assert transaction.valid == true
        assert transaction.confirmations == 1
    }

    /**
     * Executes the "send to owner" command. It catches exceptions and can expect them.
     */
    def executeSendToOwners(Address address, CurrencyID currency, OmniValue amount, Boolean exceptional=false) {
        Boolean thrown = false
        Sha256Hash txid = null

        try {
            txid = omniSendSTO(address, currency, amount)
        } catch(Exception) {
            thrown = true
        }

        assert thrown == exceptional

        return txid
    }

    /**
     * Skips tests which involve reserved token amounts, as such tests require the token/token exchange.
     */
    def maybeSkipReservedMetaDexTests(def amountReserved, def amountReservedOwners) {
        if (amountReserved > 0) {
            throw new AssumptionViolatedException("skipped")
        }
        if (amountReservedOwners.sum() > 0) {
            throw new AssumptionViolatedException("skipped")
        }
    }

    void assertBalance(Address address, CurrencyID currency, OmniValue expectedAvailable, OmniValue expectedReserved) {
        assertBalance(address,currency,expectedAvailable.bigDecimalValue(),expectedReserved.bigDecimalValue())
    }

    /**
     * Short cut to confirm a balance.
     */
    void assertBalance(Address address, CurrencyID currency, BigDecimal expectedAvailable, BigDecimal expectedReserved) {
        def balance = omniGetBalance(address, currency)
        assert balance.balance == expectedAvailable
        assert balance.reserved == expectedReserved
    }

}
