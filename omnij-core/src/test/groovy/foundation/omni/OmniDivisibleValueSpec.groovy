package foundation.omni

import spock.lang.Specification
import spock.lang.Unroll


/**
 * Test specification for OmniDivisibleValue class
 */
class OmniDivisibleValueSpec extends Specification {

    @Unroll
    def "When created from willets, resulting value is scaled correctly #willets -> #expectedValue" (Long willets, BigDecimal expectedValue) {
        when: "we try to create an OmniValue using a valid numeric type"
        OmniValue value = OmniDivisibleValue.fromWillets(willets)

        then: "it is created correctly"
        value.bigDecimalValue() == expectedValue

        where:
        willets                 | expectedValue
        0                       | 0
        1                       | 0.00000001
        100                     | 0.00000100
        10000000                | 0.1
        100000000               | 1.0
        9223372036854775807     | 92233720368.54775807
    }

    @Unroll
    def "constructor will allow a variety of integer types: #val (#type)" (Object val, Class type) {
        when: "we try to create an OmniValue using a valid numeric type"
        OmniValue value = new OmniDivisibleValue(val)

        then: "it is created correctly"
        value.longValue() == 1

        where:
        val << [1 as Short, 1, 1I, 1L]
        type = val.class
    }

    def "constructor will allow a variety of integer numeric types (with class check)"() {
        when: "we try to create a OmniValue using a valid numeric type"
        OmniValue value = new OmniDivisibleValue(val)

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

    def "constructor will allow a variety of decimal numeric types (with class check)"() {
        when: "we try to create a OmniValue using a valid numeric type"
        OmniValue value = new OmniDivisibleValue(val)

        then: "it is created correctly"
        value.bigDecimalValue() == decimalValue

        and: "class was as expected"
        val.class == valClass

        where:
        val         | decimalValue| valClass
        0           | 0G          | Integer.class
        1 as Short  | 1G          | Short.class
        1           | 1G          | Integer.class
        1I          | 1G          | Integer.class
        1L          | 1G          | Long.class
        2147483647  | 2147483647G | Integer.class
        2147483648  | 2147483648G | Long.class
        4294967295  | 4294967295G | Long.class
        1G          | 1G          | BigInteger.class
    }

    def "constructor works with a range of values"() {
        when: "we try to create a OmniValue using a valid numeric type"
        OmniDivisibleValue value = new OmniDivisibleValue(val)

        then: "it is created correctly"
        value.bigDecimalValue() == val

        where:
        val << [0,
                0.00000001,
                92233720368.54775807]
    }

    // TODO: Should these all throw the same exception type?
    @Unroll
    def "constructor invalid value: #val throws #exception"() {
        when: "we try to create a OmniValue using a value that is out of range"
        OmniDivisibleValue value = new OmniDivisibleValue(val)

        then: "Exception is thrown"
        Exception e = thrown()
        e.class == exception

        where:
        val                             | exception
        -1                              | NumberFormatException.class
        -0.000000001                    | ArithmeticException.class
        -0.00000001                     | NumberFormatException.class
        0.00000000999                   | ArithmeticException.class
        0.000000009999999999999999999   | ArithmeticException.class
        0.000000001                     | ArithmeticException.class
        1.00000000999                   | ArithmeticException.class
        1.000000009999999999999999999   | ArithmeticException.class
        1.000000001                     | ArithmeticException.class
        92233720368.54775808            | ArithmeticException.class
        92233720369                     | NumberFormatException.class
    }

    def "We can't create an OmniValue with an invalid value"() {
        when: "we try to create a OmniValue with an invalid value"
        OmniValue value = new OmniDivisibleValue(val)

        then: "exception is thrown"
        NumberFormatException e = thrown()

        where:
        val << [-1, 9223372036854775808L]
    }

    def "Converting to float not allowed"() {
        when:
        OmniValue value = new OmniDivisibleValue(1)
        def v = value.floatValue()

        then:
        UnsupportedOperationException e = thrown()
    }

    def "Converting to double not allowed"() {
        when:
        OmniValue value = new OmniDivisibleValue(1)
        def v = value.doubleValue()

        then:
        UnsupportedOperationException e = thrown()
    }

    def "Exception is thrown when converting to int would throw exception"() {
        when:
        OmniValue value = new OmniDivisibleValue(OmniValue.MAX_VALUE)
        def v = value.intValue()

        then:
        NumberFormatException e = thrown()
    }

    def "Exception is thrown when converting to int (via Groovy 'as') would throw exception"() {
        when:
        OmniValue value = new OmniDivisibleValue(OmniValue.MAX_VALUE)
        def v = value as Integer

        then:
        NumberFormatException e = thrown()
    }

}