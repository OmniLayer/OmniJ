package org.mastercoin

import spock.lang.Specification

/**
 * User: sean
 * Date: 7/12/14
 * Time: 5:36 PM
 */
class CurrencyIDSpec extends Specification {
    def "MSC has value 1"() {
        when: "we create MSC"
        CurrencyID currency = new CurrencyID(1)

        then: "the value is 1"
        currency == CurrencyID.MSC
        currency.longValue() == CurrencyID.MSC_VALUE
        currency.byteValue() == (byte) 1
        currency.shortValue() == (short) 1
        currency.intValue() == 1
        currency.longValue() == 1L
        currency.floatValue() == 1.0F
        currency.doubleValue() == 1.0D

        and: "it is in the right ecosystem"
        currency.ecosystem() == Ecosystem.MSC
    }

    def "TMSC has value 2"() {
        when: "we create TMSC"
        CurrencyID currency = new CurrencyID(2)

        then: "the value is 2"
        currency == CurrencyID.TMSC
        currency.longValue() == CurrencyID.TMSC_VALUE
        currency.byteValue() == (byte) 2
        currency.shortValue() == (short) 2
        currency.intValue() == 2
        currency.longValue() == 2L
        currency.floatValue() == 2.0F
        currency.doubleValue() == 2.0D

        and: "it is in the right ecosystem"
        currency.ecosystem() == Ecosystem.TMSC
    }

    def "An instance knows which ecosystem it is in"() {
        when: "we try to create a CurrencyID with the specified value"
        CurrencyID currency = new CurrencyID(id)

        then: "it's in the correct ecosystem"
        currency.ecosystem() == ecosystem

        where:
        id | ecosystem
        1 | Ecosystem.MSC
        2 | Ecosystem.TMSC
        3 | Ecosystem.MSC
        2147483647 | Ecosystem.MSC
        2147483647L + 1 | Ecosystem.TMSC
        4294967295 | Ecosystem.TMSC
    }

    def "We can't create an CurrencyID with an invalid value"() {
        when: "we try to create a CurrencyID with an invalid value"
        Ecosystem ecosystem = new CurrencyID(id)

        then: "exception is thrown"
        NumberFormatException e = thrown()

        where:
        id << [0, -1, 4294967296]
    }

}
