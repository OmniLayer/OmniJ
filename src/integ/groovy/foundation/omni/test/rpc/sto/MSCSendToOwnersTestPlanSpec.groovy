package foundation.omni.test.rpc.sto

import com.google.bitcoin.core.Address
import com.msgilligan.bitcoin.BTC
import com.xlson.groovycsv.CsvParser
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import spock.lang.Shared
import spock.lang.Unroll

/**
 *
 */
class MSCSendToOwnersTestPlanSpec extends BaseRegTestSpec {
    final static BigDecimal startBTC = 0.1

    @Shared
    def testdata

    def setupSpec() {
        def path = "src/integ/groovy/foundation/omni/test/rpc/sto/sto-testplan.tsv"
        def file = new File(path)
        def csv = file.text
        def data = new CsvParser().parse(csv, separator: '\t')
        testdata = data
    }

    @Unroll
    def "#description"() {
        setup:
        assert numOwners == amountAvailableOwners.size()
        assert numOwners == amountReservedOwners.size()
        assert numOwners == expectedAmountAvailableOwners.size()
        assert numOwners == expectedAmountReservedOwners.size()

        if (validity != 1) {
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }
        if (amountReserved > 0) {
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }
        if (tmscReserved > 0) {
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }
        if (amountReservedOwners.sum() > 0) {
            throw new org.junit.internal.AssumptionViolatedException("skipped")
        }

        def actorAddress = createFundedAddress(startBTC, tmscAvailable)
        def currencyMSC = new CurrencyID(ecosystem)
        def currencySP = createStoProperty(actorAddress, data)
        def owners = [] as List<Address>
        def ownerIds = 0..<numOwners

        when: "the owners are funded"
        ownerIds.each { owners << newAddress }
        owners = owners.sort { it.toString() }
        ownerIds.each { send_MP(actorAddress, owners[it], currencySP, amountAvailableOwners[it]) }
        generateBlock()

        then: "the actor has a balance of #inputMSC and #inputSP"
        getbalance_MP(actorAddress, currencyMSC).balance == tmscAvailable
        getbalance_MP(actorAddress, currencySP).balance == amountAvailable

        and: "all owners have their starting balances"
        for (id in ownerIds) {
            getbalance_MP(owners[id], currencySP).balance == amountAvailableOwners[id]
        }

        when: "#stoAmountSP is sent to owners of #currencySP"
        sendToOwnersMP(actorAddress, currencySP, amountSTO)
        generateBlock()

        then: "the sender ends up with #expectedMSC and #expectedSP"
        getbalance_MP(actorAddress, currencyMSC).balance == expectedTMSCAvailable
        getbalance_MP(actorAddress, currencySP).balance == expectedAmountAvailable

        and: "every owner has the expected balances"
        for (id in ownerIds) {
            getbalance_MP(owners[id], currencySP).balance == expectedAmountAvailableOwners[id]
        }

        where:
        data << testdata
        description = data.Description
        ecosystem = Eval.me(data.Ecosystem)
        propertyType = Eval.me(data.PropertyType)
        propertyName = data.PropertyName
        amountAvailable = Eval.me(data.AmountAvailable)
        amountReserved = Eval.me(data.AmountReserved)
        amountSTO = Eval.me(data.AmountSTO)
        tmscAvailable = Eval.me(data.TMSCAvailable)
        tmscReserved = Eval.me(data.TMSCReserved)
        numOwners = Eval.me(data.NumOwners)
        amountAvailableOwners = Eval.me(data.AmountAvailableOwners)
        amountReservedOwners = Eval.me(data.AmountReservedOwners)
        validity = Eval.me(data.Validity)
        expectedAmountAvailable = Eval.me(data.ExpectedAmountAvailable)
        expectedAmountReserved = Eval.me(data.ExpectedAmountReserved)
        expectedTMSCAvailable = Eval.me(data.ExpectedTMSCAvailable)
        expectedTMSCReserved = Eval.me(data.ExpectedTMSCReserved)
        expectedAmountAvailableOwners = Eval.me(data.ExpectedAmountAvailableOwners)
        expectedAmountReservedOwners = Eval.me(data.ExpectedAmountReservedOwners)
    }

    def createStoProperty(def actorAddress, def data) {
        def amountAvailableOwners = Eval.me(data.AmountAvailableOwners)
        def ecosystemR = Eval.me(data.Ecosystem)
        def propertyType = Eval.me(data.PropertyType)
        def amountAvailable = Eval.me(data.AmountAvailable)

        def ecosystem = new Ecosystem(ecosystemR)
        def divisibility = new PropertyType(propertyType)
        def numberOfTokens = amountAvailableOwners.sum() + amountAvailable

        if (divisibility == PropertyType.DIVISIBLE) {
            numberOfTokens = BTC.btcToSatoshis(numberOfTokens)
        }

        def txid = createProperty(actorAddress, ecosystem, divisibility, numberOfTokens.longValue())
        generateBlock()

        def transaction = getTransactionMP(txid)
        assert transaction.valid == true
        assert transaction.confirmations == 1

        def currencyID = new CurrencyID(transaction.propertyid)
        return currencyID
    }

}
