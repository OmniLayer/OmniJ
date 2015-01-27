package foundation.omni.test.rpc.sto

import com.google.bitcoin.core.Address
import com.google.bitcoin.core.Sha256Hash
import com.msgilligan.bitcoin.BTC
import com.xlson.groovycsv.CsvParser
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Data driven tests for the "send to owners" transaction type
 */
class MSCSendToOwnersTestPlanSpec extends BaseRegTestSpec {
    final static BigDecimal startBTC = 0.1

    @Shared
    def testdata

    def setupSpec() {
        def file = new File(getTestPlanPath())
        def tsv = file.text
        def data = new CsvParser().parse(tsv, separator: '\t')
        testdata = data
    }

    def getTestPlanPath() {
        return "src/integ/groovy/foundation/omni/test/rpc/sto/sto-testplan.tsv"
    }

    @Unroll
    def "#description"() {
        assert numberOfOwners == sptAvailableOwners.size()
        assert numberOfOwners == sptReservedOwners.size()
        assert numberOfOwners == expectedSPTAvailableOwners.size()
        assert numberOfOwners == expectedSPTReservedOwners.size()

        maybeSkipReservedMetaDexTests(sptReserved, sptReservedOwners)

        given:
        def startMSC = mscAvailable + mscReserved
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def currencyMSC = new CurrencyID(ecosystem.longValue())
        def currencySPT = createStoProperty(actorAddress, data)

        // Create a DEx offer to reserve an amount
        if (mscReserved > 0) {
            reserveAmountMSC(actorAddress, currencyMSC, mscReserved)
        }

        when: "the owners are funded"
        def owners = [] as List<Address>
        def ownerIds = 0..<numberOfOwners
        ownerIds.each { owners << newAddress }
        owners = owners.sort { it.toString() }
        ownerIds.each { send_MP(actorAddress, owners[it], currencySPT, sptAvailableOwners[it]) }
        generateBlock()

        then: "the actor starts with the correct #currencySPT and #currencyMSC balance"
        assertBalance(actorAddress, currencyMSC, mscAvailable, mscReserved)
        assertBalance(actorAddress, currencySPT, sptAvailable, sptReserved)

        and: "every owner starts with the correct #currencySPT balance"
        for (id in ownerIds) {
            assertBalance(owners[id], currencySPT, sptAvailableOwners[id], sptReservedOwners[id])
        }

        when: "#amountSTO is sent to owners of #currencySPT"
        def txid = executeSendToOwners(actorAddress, currencySPT, propertyType, amountSTO, expectException)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        if (txid != null) {
            def transaction = getTransactionMP(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        and: "the sender ends up with the expected #currencySPT and #currencyMSC balance"
        assertBalance(actorAddress, currencyMSC, expectedMSCAvailable, expectedMSCReserved)
        assertBalance(actorAddress, currencySPT, expectedSPTAvailable, expectedSPTReserved)

        and: "every owner ends up with the expected #currencySPT balance"
        for (id in ownerIds) {
            assertBalance(owners[id], currencySPT, expectedSPTAvailableOwners[id], expectedSPTReservedOwners[id])
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
        def startMSC = 2.0
        def expectException = true
        def expectedValidity = false

        def actorAddress = createFundedAddress(startBTC, startMSC)
        def currencyMSC = new CurrencyID(ecosystem.longValue())
        def currencySPT = new CurrencyID(4294967295L) // does not exist

        given: "the actor starts with #startMSC #currencyMSC"
        assert getbalance_MP(actorAddress, currencyMSC).balance == startMSC

        when: "#amountSTO is sent to owners of #currencySPT"
        def txid = executeSendToOwners(actorAddress, currencySPT, propertyType, amountSTO, expectException)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        if (txid != null) {
            def transaction = getTransactionMP(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        and: "the sender's balance is still the same"
        getbalance_MP(actorAddress, currencyMSC).balance == startMSC
    }

    @Ignore("https://github.com/msgilligan/bitcoin-spock/issues/35")
    def "STO Property ID is 0 - bitcoin"() {
        def ecosystem = Ecosystem.TMSC
        def propertyType = PropertyType.DIVISIBLE
        def btcAvailable = 0.001
        def btcAvailableOwners = 1.0
        def amountSTO = 0.0001
        def startMSC = 2.0
        def expectException = true
        def expectedValidity = false
        def currencyMSC = new CurrencyID(ecosystem.longValue())

        when: "there is a well funded actor and two owners with bitcoin"
        def actorAddress = createFundedAddress(btcAvailable, startMSC)
        def ownerA = createFundedAddress(btcAvailableOwners, startMSC)
        def ownerB = createFundedAddress(btcAvailableOwners, startMSC)

        then: "they have a certain amount of tokens and coins"
        getbalance_MP(actorAddress, currencyMSC).balance == startMSC
        getBitcoinBalance(actorAddress) == btcAvailable
        getBitcoinBalance(ownerA) == btcAvailableOwners
        getBitcoinBalance(ownerB) == btcAvailableOwners

        when: "#amountSTO is sent to the bitcoin owners"
        def txid = executeSendToOwners(actorAddress, CurrencyID.BTC, propertyType, amountSTO, expectException)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        if (txid != null) {
            def transaction = getTransactionMP(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        and: "the sender paid at worst 2 * #stdTxFee bitcoin for the transaction itself"
        getBitcoinBalance(actorAddress) >= (btcAvailable - 2 * stdTxFee)

        and: "all other balances are still the same"
        getbalance_MP(actorAddress, currencyMSC).balance == startMSC
        getBitcoinBalance(ownerA) == btcAvailableOwners
        getBitcoinBalance(ownerB) == btcAvailableOwners
    }

    @Ignore("https://github.com/msgilligan/bitcoin-spock/issues/35")
    def "Sender owns all the coins of the STO Property, other addresses had non-zero balances but now zero balances"() {
        def ecosystem = Ecosystem.TMSC
        def propertyType = PropertyType.DIVISIBLE
        def amountSTO = 1.0
        def startMSC = 1.0
        def expectException = true
        def expectedValidity = false
        def currencyMSC = new CurrencyID(ecosystem.longValue())

        def actorAddress = createFundedAddress(startBTC, startMSC)
        def ownerA = createFundedAddress(startBTC, startMSC)
        def ownerB = createFundedAddress(startBTC, startMSC)

        assert getbalance_MP(actorAddress, currencyMSC).balance == startMSC
        assert getbalance_MP(ownerA, currencyMSC).balance == startMSC
        assert getbalance_MP(ownerB, currencyMSC).balance == startMSC

        // Create property
        def numberOfTokens = amountSTO
        if (propertyType == PropertyType.DIVISIBLE) {
            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
        }
        def txidCreation = createProperty(ownerA, ecosystem, propertyType, numberOfTokens.longValue())
        generateBlock()
        def txCreation = getTransactionMP(txidCreation)
        assert txCreation.valid == true
        assert txCreation.confirmations == 1
        def currencySPT = new CurrencyID(txCreation.propertyid)

        // Owner A has the tokens
        assert getbalance_MP(actorAddress, currencySPT).balance == 0.0
        assert getbalance_MP(ownerA, currencySPT).balance == amountSTO
        assert getbalance_MP(ownerB, currencySPT).balance == 0.0

        // Owner A sends half of the tokens to owner B
        send_MP(ownerA, ownerB, currencySPT, (amountSTO / 2))
        generateBlock()
        assert getbalance_MP(actorAddress, currencySPT).balance == 0.0
        assert getbalance_MP(ownerA, currencySPT).balance == (amountSTO / 2)
        assert getbalance_MP(ownerB, currencySPT).balance == (amountSTO / 2)

        // Owner A and B send the tokens to the main actor
        send_MP(ownerA, actorAddress, currencySPT, (amountSTO / 2))
        send_MP(ownerB, actorAddress, currencySPT, (amountSTO / 2))
        generateBlock()
        assert getbalance_MP(actorAddress, currencySPT).balance == amountSTO
        assert getbalance_MP(ownerA, currencySPT).balance == 0.0
        assert getbalance_MP(ownerB, currencySPT).balance == 0.0

        when: "#amountSTO is sent to owners of #currencySPT"
        def txid = executeSendToOwners(actorAddress, currencySPT, propertyType, amountSTO, expectException)
        generateBlock()

        then: "the transaction validity is #expectedValidity"
        if (txid != null) {
            def transaction = getTransactionMP(txid)
            assert transaction.valid == expectedValidity
            assert transaction.confirmations == 1
        }

        and: "all balances still the same"
        assert getbalance_MP(actorAddress, currencyMSC).balance == startMSC
        assert getbalance_MP(actorAddress, currencySPT).balance == amountSTO
        assert getbalance_MP(ownerA, currencySPT).balance == 0.0
        assert getbalance_MP(ownerB, currencySPT).balance == 0.0
    }

    /**
     * Creates a new property and returns it's identifier.
     */
    def createStoProperty(Address actorAddress, def data) {
        def amountAvailableOwners = Eval.me(data.AmountAvailableOwners) as List<BigDecimal>
        def amountAvailable = new BigDecimal(data.AmountAvailable)
        def ecosystem = new Ecosystem(Short.valueOf(data.Ecosystem))
        def propertyType = new PropertyType(Integer.valueOf(data.PropertyType))

        def numberOfTokens = amountAvailable

        if (amountAvailableOwners.size()) {
            numberOfTokens += amountAvailableOwners.sum()
        }

        if (propertyType == PropertyType.DIVISIBLE) {
            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
        }

        def txid = createProperty(actorAddress, ecosystem, propertyType, numberOfTokens.longValue())
        generateBlock()

        def transaction = getTransactionMP(txid)
        assert transaction.valid == true
        assert transaction.confirmations == 1

        def currencyID = new CurrencyID(transaction.propertyid)
        return currencyID
    }

    /**
     * Creates an offer on the distributed exchange to reserve an amount.
     */
    def reserveAmountMSC(Address actorAddress, CurrencyID currency, BigDecimal amount) {
        def desiredBTC = 1.0
        def blockSpan = 100
        def commitFee = 0.0001
        def action = 1 // new offer

        def txid = createDexSellOffer(actorAddress, currency, amount, desiredBTC, blockSpan, commitFee, action)
        generateBlock()

        def transaction = getTransactionMP(txid)
        assert transaction.valid == true
        assert transaction.confirmations == 1
    }

    /**
     * Executes the "send to owner" command. It catches exceptions and can expect them.
     */
    def executeSendToOwners(Address address, def currency, def propertyType, def amount, def exceptional=false) {
        Boolean thrown = false
        Sha256Hash txid = null

        try {
            txid = sendToOwnersMP(address, currency, amount)
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
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }
        if (amountReservedOwners.sum() > 0) {
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }
    }

    /**
     * Short cut to confirm a balance.
     */
    void assertBalance(Address address, CurrencyID currency, def expectedAvailable, def expectedReserved) {
        def balance = getbalance_MP(address, currency)
        assert balance.balance == expectedAvailable
        assert balance.reserved == expectedReserved
    }

}
