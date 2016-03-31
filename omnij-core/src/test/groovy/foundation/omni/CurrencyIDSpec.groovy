package foundation.omni

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Specification for CurrencyID class
 */
class CurrencyIDSpec extends Specification {
    def "BTC has value 0"() {
        when: "we create BTC"
        CurrencyID currency = new CurrencyID(0)

        then: "the value is 0"
        currency == CurrencyID.BTC
        currency.getValue() == CurrencyID.BTC_VALUE
        currency.getValue() == 0L

        and: "it is in the right ecosystem"
        currency.ecosystem == Ecosystem.MSC     // Is this right???
    }

    def "MSC has value 1"() {
        when: "we create MSC"
        CurrencyID currency = new CurrencyID(1)

        then: "the value is 1"
        currency == CurrencyID.MSC
        currency.getValue() == CurrencyID.MSC_VALUE
        currency.getValue() == 1L

        and: "it is in the right ecosystem"
        currency.ecosystem == Ecosystem.MSC
    }

    def "TMSC has value 2"() {
        when: "we create TMSC"
        CurrencyID currency = new CurrencyID(2)

        then: "the value is 2"
        currency == CurrencyID.TMSC
        currency.getValue() == CurrencyID.TMSC_VALUE
        currency.getValue() == 2L

        and: "it is in the right ecosystem"
        currency.ecosystem == Ecosystem.TMSC
    }

    @Unroll
    def "An instance knows which ecosystem it is in (#id in #ecosystem)"() {
        when: "we try to create a CurrencyID with the specified value"
        CurrencyID currency = new CurrencyID(id)

        then: "it's in the correct ecosystem"
        currency.ecosystem == ecosystem

        where:
        id | ecosystem
        1 | Ecosystem.MSC
        2 | Ecosystem.TMSC
        3 | Ecosystem.MSC
        2147483647 | Ecosystem.MSC
        2147483647L + 1 | Ecosystem.TMSC
        4294967295 | Ecosystem.TMSC
    }

    @Unroll
    def "constructor will allow a variety of integer types (#id)"() {
        when: "we try to create a CurrencyID using a valid numeric type"
        CurrencyID currencyID = new CurrencyID(id)

        then: "it is created correctly"
        currencyID.getValue() == 1

        where:
        id << [1 as Short, 1, 1I, 1L]
    }

    @Unroll
    def "constructor will allow a variety of integer types (#id / #idClass)"() {
        when: "we try to create a CurrencyID using a valid numeric type"
        CurrencyID currencyID = new CurrencyID(id)

        then: "it is created correctly"
        currencyID.getValue() == longValue

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

    @Unroll
    def "constructor is strongly typed and won't allow all Number subclasses (#id)"() {
        when: "we try to create a CurrencyID using an invalid numeric type"
        CurrencyID currencyID = new CurrencyID(id)

        then: "exception is thrown"
        groovy.lang.GroovyRuntimeException e = thrown()

        where:
        id << [1F, 1.1F, 1.0D, 1.1D, 1.0, 1.1, 1.0G, 1.1G]
    }

    @Unroll
    def "constructor is strongly typed and won't allow all Number subclasses (#id / #idClass)"() {
        when: "we try to create a CurrencyID using an invalid numeric type"
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

    @Unroll
    def "We can't create an CurrencyID with an invalid value (#id)"() {
        when: "we try to create a CurrencyID with an invalid value"
        CurrencyID currencyID = new CurrencyID(id)

        then: "exception is thrown"
        IllegalArgumentException e = thrown()

        where:
        id << [-1, 4294967296]
    }

    @Unroll
    def "A CurrencyID can be represented as String (#id -> #currencyIdAsString)"() {
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

    @Unroll
    def "The String \"#str\" can be used to create CurrencyID:#value"() {

        expect:
        CurrencyID currency = CurrencyID.valueOf(str)
        currency.getValue() == value

        where:
        str            | value
        "BTC"          | CurrencyID.BTC_VALUE
        "MSC"          | CurrencyID.MSC_VALUE
        "TMSC"         | CurrencyID.TMSC_VALUE
        "MaidSafeCoin" | CurrencyID.MaidSafeCoin_VALUE
        "USDT"         | CurrencyID.USDT_VALUE
    }
}
