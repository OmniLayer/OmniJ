package foundation.omni.test.rpc.crowdsale

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import org.bitcoinj.core.Address
import spock.lang.Shared

class CloseCrowdsaleSpec extends BaseRegTestSpec {

    final static BigDecimal startBTC = 0.01
    final static BigDecimal startMSC = 1.00

    @Shared Address actorAddress
    @Shared Address otherAddress
    @Shared CurrencyID currencyID
    @Shared CurrencyID nonCrowdsaleID

    def setupSpec() {
        actorAddress = createFundedAddress(startBTC, startMSC)
        otherAddress = createFundedAddress(startBTC, startMSC)

        def txid = createCrowdsale(actorAddress, Ecosystem.TMSC, PropertyType.DIVISIBLE, CurrencyID.TMSC, 500000000L,
                                   2147483648L, 0 as Byte, 0 as Byte)
        generateBlock()
        def creationTx = getTransactionMP(txid)
        assert (creationTx.valid)
        currencyID = new CurrencyID(creationTx.propertyid as Long)
        nonCrowdsaleID = fundNewProperty(actorAddress, 500.0, PropertyType.DIVISIBLE, Ecosystem.TMSC)
    }

    def "Closing a non-existing crowdsale is invalid"() {
        when:
        def txid = closeCrowdsale(actorAddress, new CurrencyID(CurrencyID.MAX_REAL_ECOSYSTEM_VALUE))
        generateBlock()

        then:
        getTransactionMP(txid).valid == false
    }

    def "Closing a non-crowdsale is invalid"() {
        when:
        def txid = closeCrowdsale(actorAddress, nonCrowdsaleID)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false
    }

    def "A crowdsale can not be closed by a non-issuer"() {
        when:
        def txid = closeCrowdsale(otherAddress, currencyID)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false

        and:
        getcrowdsale_MP(currencyID).active
    }

    def "Before closing a crowdsale, tokens can be purchased"() {
        when:
        def txid = send_MP(otherAddress, actorAddress, CurrencyID.TMSC, 0.5)
        generateBlock()

        then:
        getTransactionMP(txid).valid

        and:
        getbalance_MP(actorAddress, CurrencyID.TMSC).balance == 1.5
        getbalance_MP(otherAddress, CurrencyID.TMSC).balance == 0.5

        and: "tokens were credited to the participant"
        getbalance_MP(actorAddress, currencyID).balance == 0.0
        getbalance_MP(otherAddress, currencyID).balance == 2.5
    }

    def "A crowdsale can be closed with transaction type 53"() {
        when:
        def txid = closeCrowdsale(actorAddress, currencyID)
        generateBlock()

        then:
        getTransactionMP(txid).valid

        and:
        getcrowdsale_MP(currencyID).active == false
    }

    def "A crowdsale can only be closed once"() {
        when:
        def txid = closeCrowdsale(actorAddress, currencyID)
        generateBlock()

        then:
        getTransactionMP(txid).valid == false
    }

    def "Sending tokens, after a crowdsale was closed, does not grant tokens"() {
        when:
        def txid = send_MP(otherAddress, actorAddress, CurrencyID.TMSC, 0.5)
        generateBlock()

        then:
        getTransactionMP(txid).valid

        and:
        getbalance_MP(actorAddress, CurrencyID.TMSC).balance == 2.0
        getbalance_MP(otherAddress, CurrencyID.TMSC).balance == 0.0

        and: "no tokens were credited to the participant"
        getbalance_MP(actorAddress, currencyID) == old(getbalance_MP(actorAddress, currencyID))
        getbalance_MP(otherAddress, currencyID) == old(getbalance_MP(otherAddress, currencyID))
    }

}
