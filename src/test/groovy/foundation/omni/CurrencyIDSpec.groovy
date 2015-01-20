package foundation.omni

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

    def "constructor will allow a variety of integer types"() {
        when: "we try to create an ecosystem using a valid numeric type"
        CurrencyID currencyID = new CurrencyID(id)

        then: "it is created correctly"
        currencyID.longValue() == 1

        where:
        id << [1 as Short, 1, 1I, 1L]
    }

    def "constructor will allow a variety of integer types (with class check)"() {
        when: "we try to create an ecosystem using a valid numeric type"
        CurrencyID currencyID = new CurrencyID(id)

        then: "it is created correctly"
        currencyID.longValue() == longValue

        and: "class was as expected"
        id.class == idClass

        where:
        id         | longValue   | idClass
        1 as Short | 1L          | Short.class
        1          | 1L          | Integer.class
        1I         | 1L          | Integer.class
        1L         | 1L          | Long.class
        2147483647 | 2147483647L | Integer.class
        2147483648 | 2147483648L | Long.class
        4294967295 | 4294967295L | Long.class
    }

    def "constructor is strongly typed and won't allow all Number subclasses"() {
        when: "we try to create an ecosystem using an invalid numeric type"
        CurrencyID currencyID = new CurrencyID(id)

        then: "exception is thrown"
        groovy.lang.GroovyRuntimeException e = thrown()

        where:
        id << [1F, 1.1F, 1.0D, 1.1D, 1.0, 1.1, 1.0G, 1.1G]
    }

    def "constructor is strongly typed and won't allow all Number subclasses (with class check)"() {
        when: "we try to create an ecosystem using an invalid numeric type"
        CurrencyID currencyID = new CurrencyID(id)

        then: "exception is thrown"
        groovy.lang.GroovyRuntimeException e = thrown()

        and: "class was as expected"
        id.class == idClass

        where:
        id   | idClass
        1F   | Float.class
        1.1F | Float.class
        1.0D | Double.class
        1.1D | Double.class
        1G   | BigInteger.class
        1.0  | BigDecimal.class
        1.1  | BigDecimal.class
        1.0G | BigDecimal.class
        1.1G | BigDecimal.class
    }

    def "We can't create an CurrencyID with an invalid value"() {
        when: "we try to create a CurrencyID with an invalid value"
        CurrencyID currencyID = new CurrencyID(id)

        then: "exception is thrown"
        NumberFormatException e = thrown()

        where:
        id << [0, -1, 4294967296]
    }

    def "A CurrencyID can be represented as String"() {
        expect:
        CurrencyID currency = new CurrencyID(id)
        currency.toString() == currencyIdAsString

        where:
        id | currencyIdAsString
        1 | "CurrencyID:1"
        2 | "CurrencyID:2"
        2147483647 | "CurrencyID:2147483647"
        2147483647L + 1 | "CurrencyID:2147483648"
        4294967295 | "CurrencyID:4294967295"
    }

}
