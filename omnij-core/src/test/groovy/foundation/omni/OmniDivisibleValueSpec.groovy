package foundation.omni

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll


/**
 * Test specification for OmniDivisibleValue class
 */
class OmniDivisibleValueSpec extends Specification {

    @Unroll
    def "When created from willets, resulting value is scaled correctly #willets -> #expectedValue" (Long willets, BigDecimal expectedValue) {
        when: "we try to create an OmniValue using a valid numeric type"
        OmniValue value = OmniDivisibleValue.ofWillets(willets)

        then: "it is created correctly"
        value.numberValue() == expectedValue

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
        OmniValue value = OmniDivisibleValue.of(val)

        then: "it is created correctly"
        value.longValue() == 1

        where:
        val << [1 as Short, 1, 1I, 1L]
        type = val.class
    }

    def "constructor will allow a variety of integer numeric types (with class check)"() {
        when: "we try to create a OmniValue using a valid numeric type"
        OmniValue value = OmniDivisibleValue.of(val)

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
        OmniValue value = OmniDivisibleValue.of(val)

        then: "it is created correctly"
        value.numberValue().toBigInteger() == decimalValue

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
        OmniDivisibleValue value = OmniDivisibleValue.of(val)

        then: "it is created correctly"
        value.numberValue() == val

        where:
        val << [0,
                0.00000001,
                92233720368.54775807]
    }

    // TODO: Should these all throw the same exception type?
    @Unroll
    def "constructor invalid value: #val throws #exception"() {
        when: "we try to create a OmniValue using a value that is out of range"
        OmniDivisibleValue value = OmniDivisibleValue.of(val)

        then: "Exception is thrown"
        Exception e = thrown()
        e.class == exception

        where:
        val                             | exception
        -1                              | ArithmeticException.class
        -0.000000001                    | ArithmeticException.class
        -0.00000001                     | ArithmeticException.class
        0.00000000999                   | ArithmeticException.class
        0.000000009999999999999999999   | ArithmeticException.class
        0.000000001                     | ArithmeticException.class
        1.00000000999                   | ArithmeticException.class
        1.000000009999999999999999999   | ArithmeticException.class
        1.000000001                     | ArithmeticException.class
        92233720368.54775808            | ArithmeticException.class
        92233720369                     | ArithmeticException.class
    }

    def "We can't create an OmniValue with an invalid value"() {
        when: "we try to create a OmniValue with an invalid value"
        OmniValue value = OmniDivisibleValue.of(val)

        then: "exception is thrown"
        ArithmeticException e = thrown()

        where:
        val << [-1, 9223372036854775808L]
    }

    def "Converting to float not allowed"() {
        when:
        OmniValue value = OmniDivisibleValue.of(1)
        def v = value.floatValue()

        then:
        UnsupportedOperationException e = thrown()
    }

    def "Converting to double not allowed"() {
        when:
        OmniValue value = OmniDivisibleValue.of(1)
        def v = value.doubleValue()

        then:
        UnsupportedOperationException e = thrown()
    }

    def "Exception is thrown when converting to int would throw exception"() {
        when:
        OmniValue value = OmniDivisibleValue.of(OmniValue.MAX_VALUE)
        def v = value.intValue()

        then:
        ArithmeticException e = thrown()
    }

    def "Exception is thrown when converting to int (via Groovy 'as') would throw exception"() {
        when:
        OmniValue value = OmniDivisibleValue.of(OmniValue.MAX_VALUE)
        def v = value as Integer

        then:
        ArithmeticException e = thrown()
    }

    @Unroll
    def "Addition works: #a + #b == #c"(BigDecimal a, BigDecimal b, BigDecimal c) {
        when:
        def A = OmniDivisibleValue.of(a)
        def B = OmniDivisibleValue.of(b)
        def C = OmniDivisibleValue.of(c)

        then:
        C.numberValue() == (A + B).numberValue()

        where:
        a | b | c
        1 | 1 | 2
        1.1 | 1.1 | 2.2
    }

    @Unroll
    def "Multiplication works: #a * #b == #c"(BigDecimal a, long b, BigDecimal c) {
        when:
        def A = OmniDivisibleValue.of(a)
        def C = OmniDivisibleValue.of(c)

        then:
        C.numberValue() == (A * b).numberValue()

        where:
        a | b | c
        2 | 3 | 6
        2.1 | 3 | 6.3
    }


    @Unroll
    def "Division works: #a / #b == #c"(BigDecimal a, long b, BigDecimal c) {
        when:
        def A = OmniDivisibleValue.of(a)
        def C = OmniDivisibleValue.of(c)

        then:
        C.numberValue() == (A / b).numberValue()

        where:
        a | b | c
        6 | 3 | 2
        6.3 | 3 | 2.1
    }

    def "Equality works"() {
        expect:
        OmniDivisibleValue.of(1) == OmniDivisibleValue.of(1)
//        OmniDivisibleValue.of(0.1) == OmniDivisibleValue.of(0.1)    // Broken!!! is this a groovy bug?
    }

}