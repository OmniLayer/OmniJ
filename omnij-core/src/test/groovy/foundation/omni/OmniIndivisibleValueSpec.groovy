package foundation.omni

import spock.lang.Specification


/**
 * Test specification for OmniIndivisibleValue class
 */
class OmniIndivisibleValueSpec extends Specification {

    def "constructor will allow a variety of integer types"() {
        when: "we try to create an OmniValue using a valid numeric type"
        OmniValue value = new OmniIndivisibleValue(val)

        then: "it is created correctly"
        value.longValue() == 1

        where:
        val << [1 as Short, 1, 1I, 1L]
    }

    def "constructor will allow a variety of integer types (with class check)"() {
        when: "we try to create a OmniValue using a valid numeric type"
        OmniValue value = new OmniIndivisibleValue(val)

        then: "it is created correctly"
        value.longValue() == longValue

        and: "class was as expected"
        val.class == valClass

        where:
        val        | longValue   | valClass
        1 as Short | 1L          | Short.class
        1          | 1L          | Integer.class
        1I         | 1L          | Integer.class
        1L         | 1L          | Long.class
        2147483647 | 2147483647L | Integer.class
        2147483648 | 2147483648L | Long.class
        4294967295 | 4294967295L | Long.class
        1G         | 1L          | BigInteger.class
    }

    def "constructor is strongly typed and won't allow all Number subclasses"() {
        when: "we try to create a OmniValue using an invalid numeric type"
        OmniValue value = new OmniIndivisibleValue(val)

        then: "exception is thrown"
        groovy.lang.GroovyRuntimeException e = thrown()

        where:
        val << [1F, 1.1F, 1.0D, 1.1D, 1.0, 1.1, 1.0G, 1.1G]
    }

    def "constructor is strongly typed and won't allow all Number subclasses (with class check)"() {
        when: "we try to create a OmniValue using an invalid numeric type"
        OmniValue value = new OmniIndivisibleValue(val)

        then: "exception is thrown"
        GroovyRuntimeException e = thrown()

        and: "class was as expected"
        val.class == valClass

        where:
        val  | valClass
        1F   | Float.class
        1.1F | Float.class
        1.0D | Double.class
        1.1D | Double.class
        1.0  | BigDecimal.class
        1.1  | BigDecimal.class
        1.0G | BigDecimal.class
        1.1G | BigDecimal.class
    }

    def "We can't create an OmniValue with an invalid value"() {
        when: "we try to create a OmniValue with an invalid value"
        OmniValue value = new OmniIndivisibleValue(val)

        then: "exception is thrown"
        NumberFormatException e = thrown()

        where:
        val << [-1, 9223372036854775808L]
    }

    def "Converting to float not allowed"() {
        when:
        OmniValue value = new OmniIndivisibleValue(1)
        def v = value.floatValue()

        then:
        UnsupportedOperationException e = thrown()
    }

    def "Converting to double not allowed"() {
        when:
        OmniValue value = new OmniIndivisibleValue(1)
        def v = value.doubleValue()

        then:
        UnsupportedOperationException e = thrown()
    }

    def "Exception is thrown when converting to int would throw exception"() {
        when:
        OmniValue value = new OmniIndivisibleValue(OmniValue.MAX_VALUE)
        def v = value.intValue()

        then:
        ArithmeticException e = thrown()
    }

    def "Exception is thrown when converting to int (via Groovy 'as') would throw exception"() {
        when:
        OmniValue value = new OmniIndivisibleValue(OmniValue.MAX_VALUE)
        def v = value as Integer

        then:
        ArithmeticException e = thrown()
    }

}