package foundation.omni

import spock.lang.Specification


/**
 * Tests for OmniAmount
 */
class OmniAmountSpec extends Specification {
    def "can create with value and currency id"() {
        given:
        OmniValue value = OmniDivisibleValue.of(1.0)

        when: "we create an OmniAmount"
        OmniAmount amount = new OmniAmount(value, CurrencyID.OMNI)

        then:
        amount.number == value
    }

}