package foundation.omni

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll


/**
 * Test specification for OmniDivisibleValue class
 */
class OmniDivisibleValueSpec extends Specification {

    def "constructor will allow a variety of integer types"() {
        when: "we try to create an OmniValue using a valid numeric type"
        OmniValue value = new OmniDivisibleValue(val)

        then: "it is created correctly"
        value.longValue() == 1

        where:
        val << [1 as Short, 1, 1I, 1L]
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

    //@Ignore("Groovy is converting these to BigDecimal which is allowed, though the 1.1 values are failing")
    @Unroll
    def "constructor is strongly typed and won't allow all Number subclasses: #val"() {
        when: "we try to create a OmniValue using an invalid numeric type"
        OmniValue value = new OmniDivisibleValue(val)

        then: "exception is thrown"
        ArithmeticException e = thrown()

        where:
        val << [1F, 1.1F, 1.0D, 1.1D]
    }

    //@Ignore("Groovy is converting these to BigDecimal which is allowed, though the 1.1 values are failing")
    @Unroll
    def "constructor is strongly typed and won't allow all Number subclasses: #val, #valClass"() {
        when: "we try to create a OmniValue using an invalid numeric type"
        OmniValue value = new OmniDivisibleValue(val)

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