package foundation.omni.test.rpc.smartproperty

import org.bitcoinj.core.Address
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import spock.lang.Shared

/*
  Spreadsheet with test transactions for Smart Property:
  https://docs.google.com/spreadsheet/ccc?key=0Al4FhV693WqWdGNFRDhEMTFtaWNmcVNObFlVQmNOa1E&usp=drive_web#gid=0
  Curtis may have sent some of these into the BlockChain
 */

class SmartPropertySpec extends BaseRegTestSpec {

    final static BigDecimal startBTC = 1.0
    final static BigDecimal startMSC = 50.0

    @Shared
    Address fundedAddress

    def setup() {
        fundedAddress = createFundedAddress(startBTC, startMSC)
    }

    def "Create smart property with divisible units"() {
        when: "we transmit a property creation transaction in main ecosystem"
        def txid = createProperty(fundedAddress, Ecosystem.MSC, PropertyType.DIVISIBLE, 314159265L)

        and: "a new block is mined"
        client.generateBlock()

        then: "the transaction confirms"
        def transaction = client.omniGetTransaction(txid)
        transaction.confirmations == 1

        and: "it should be valid"
        transaction.valid == true

        and: "is in the main ecosystem"
        def propertyId = new CurrencyID(transaction.propertyid)
        propertyId.ecosystem == Ecosystem.MSC

        and: "has 3.14159265 divisible units"
        transaction.divisible == true
        transaction.amount == "3.14159265"

        and: "this amount was credited to the issuer"
        def available = omniGetBalance(fundedAddress, propertyId)
        available.balance == 3.14159265
    }

    def "Create test property with indisivible units"() {
        when: "we transmit a property creation transaction in test ecosystem"
        def txid = createProperty(fundedAddress, Ecosystem.TMSC, PropertyType.INDIVISIBLE, 4815162342L)

        and: "a new block is mined"
        client.generateBlock()

        then: "the transaction confirms"
        def transaction = client.omniGetTransaction(txid)
        transaction.confirmations == 1

        and: "it should be valid"
        transaction.valid == true

        and: "is in the test ecosystem"
        def propertyId = new CurrencyID(transaction.propertyid)
        propertyId.ecosystem == Ecosystem.TMSC

        and: "has 4815162342 indivisible units"
        transaction.divisible == false
        transaction.amount == "4815162342"

        and: "this amount was credited to the issuer"
        def available = omniGetBalance(fundedAddress, propertyId)
        available.balance == 4815162342L
    }

}
