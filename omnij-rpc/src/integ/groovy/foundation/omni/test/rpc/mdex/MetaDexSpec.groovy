package foundation.omni.test.rpc.mdex

import com.msgilligan.bitcoin.BTC
import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import org.bitcoinj.core.Address
import org.junit.internal.AssumptionViolatedException
import spock.lang.Unroll

/**
 * Specification for the distributed token exchange
 */
class MetaDexSpec extends BaseRegTestSpec {
    final static BigDecimal startBTC = 0.1
    final static BigDecimal startMSC = 0.1
    final static Byte actionNew = 1

    @Unroll
    def "A new offer of #amountForSale #typeForSale for #amountDesired #propertyDesired can be created"() {
        def actorAddress = createFundedAddress(startBTC, startMSC)
        def propertyForSale = fundNewProperty(actorAddress, amountForSale, typeForSale, propertyDesired.ecosystem)

        expect: "no offers for the new property"
        getorderbook_MP(propertyForSale).size() == 0
        getorderbook_MP(propertyForSale, propertyDesired).size() == 0

        when: "creating an offer with action = 1"
        def txid = trade_MP(actorAddress, propertyForSale, amountForSale, propertyDesired, amountDesired, actionNew)
        generateBlock()

        then: "it is a valid transaction"
        def plainTx = getTransactionMP(txid)
        plainTx.confirmations == 1
        plainTx.valid == true

        and: "it is also a valid order"
        def tradeTx = gettrade_MP(txid)
        tradeTx.confirmations == 1
        tradeTx.valid == true

        and: "there is now an offering for the new property in the orderbook"
        def orderbook = getorderbook_MP(propertyForSale, propertyDesired)
        orderbook.size() == 1

        where:
        typeForSale              | amountForSale          | propertyDesired  | amountDesired
        PropertyType.INDIVISIBLE | new BigDecimal("7")    | CurrencyID.MSC   | new BigDecimal("23.0") // MSC
        PropertyType.DIVISIBLE   | new BigDecimal("42.0") | CurrencyID.TMSC  | new BigDecimal("16.0") // TMSC
    }

    CurrencyID fundNewProperty(Address address, BigDecimal amount, PropertyType type, Ecosystem ecosystem) {
        if (type == PropertyType.DIVISIBLE) {
            amount = BTC.btcToSatoshis(amount)
        }
        def txidCreation = createProperty(address, ecosystem, type, amount.longValue())
        generateBlock()
        def txCreation = getTransactionMP(txidCreation)
        assert txCreation.valid == true
        assert txCreation.confirmations == 1
        return new CurrencyID(txCreation.propertyid as long)
    }

    def setupSpec() {
        if (!commandExists("gettrade_MP")) {
            throw new AssumptionViolatedException('The client has no "gettrade_MP" command')
        }
        if (!commandExists("getorderbook_MP")) {
            throw new AssumptionViolatedException('The client has no "getorderbook_MP" command')
        }
    }

}
