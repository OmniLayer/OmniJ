package foundation.omni.test.rpc.crowdsale

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.PropertyType
import org.bitcoinj.core.Address
import spock.lang.Shared

class CloseCrowdsaleSpec extends BaseRegTestSpec {

    final static startBTC = 0.01.btc
    final static startMSC = 1.divisible

    @Shared Address actorAddress
    @Shared Address otherAddress
    @Shared CurrencyID currencyID
    @Shared CurrencyID nonCrowdsaleID

    def setupSpec() {
        actorAddress = createFundedAddress(startBTC, startMSC)
        otherAddress = createFundedAddress(startBTC, startMSC)

        def txid = createCrowdsale(actorAddress, Ecosystem.TOMNI, PropertyType.DIVISIBLE, CurrencyID.TOMNI, 500000000L,
                                   2147483648L, 0 as Byte, 0 as Byte)
        generate()
        def creationTx = omniGetTransaction(txid)
        assert (creationTx.valid)
        currencyID = new CurrencyID(creationTx.propertyid as Long)
        nonCrowdsaleID = fundNewProperty(actorAddress, 500.divisible, Ecosystem.TOMNI)
    }

    def "Closing a non-existing crowdsale is invalid"() {
        when:
        def txid = closeCrowdsale(actorAddress, new CurrencyID(CurrencyID.MAX_REAL_ECOSYSTEM_VALUE))
        generate()

        then:
        omniGetTransaction(txid).valid == false
    }

    def "Closing a non-crowdsale is invalid"() {
        when:
        def txid = closeCrowdsale(actorAddress, nonCrowdsaleID)
        generate()

        then:
        omniGetTransaction(txid).valid == false
    }

    def "A crowdsale can not be closed by a non-issuer"() {
        when:
        def txid = closeCrowdsale(otherAddress, currencyID)
        generate()

        then:
        omniGetTransaction(txid).valid == false

        and:
        omniGetCrowdsale(currencyID).active
    }

    def "Before closing a crowdsale, tokens can be purchased"() {
        when:
        def txid = omniSend(otherAddress, actorAddress, CurrencyID.TOMNI, 0.5.divisible)
        generate()

        then:
        omniGetTransaction(txid).valid

        and:
        omniGetBalance(actorAddress, CurrencyID.TOMNI).balance.equals(1.5.divisible)
        omniGetBalance(otherAddress, CurrencyID.TOMNI).balance.equals(0.5.divisible)

        and: "tokens were credited to the participant"
        omniGetBalance(actorAddress, currencyID).balance.equals(0.divisible)
        omniGetBalance(otherAddress, currencyID).balance.equals(2.5.divisible)
    }

    def "A crowdsale can be closed with transaction type 53"() {
        when:
        def txid = closeCrowdsale(actorAddress, currencyID)
        generate()

        then:
        omniGetTransaction(txid).valid

        and:
        omniGetCrowdsale(currencyID).active == false
    }

    def "A crowdsale can only be closed once"() {
        when:
        def txid = closeCrowdsale(actorAddress, currencyID)
        generate()

        then:
        omniGetTransaction(txid).valid == false
    }

    def "Sending tokens, after a crowdsale was closed, does not grant tokens"() {
        when:
        def txid = omniSend(otherAddress, actorAddress, CurrencyID.TOMNI, 0.5.divisible)
        generate()

        then:
        omniGetTransaction(txid).valid

        and:
        omniGetBalance(actorAddress, CurrencyID.TOMNI).balance.equals(2.divisible)
        omniGetBalance(otherAddress, CurrencyID.TOMNI).balance.equals(0.divisible)

        and: "no tokens were credited to the participant"
        omniGetBalance(actorAddress, currencyID) == old(omniGetBalance(actorAddress, currencyID))
        omniGetBalance(otherAddress, currencyID) == old(omniGetBalance(otherAddress, currencyID))
    }

}
