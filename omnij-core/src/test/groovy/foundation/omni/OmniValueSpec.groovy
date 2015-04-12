package foundation.omni

import spock.lang.Specification
import spock.lang.Unroll


/**
 * OmniValue abstract base class Specification
 */
class OmniValueSpec extends Specification {

    @Unroll
    def "checkValue does not throw exception for valid long value: #val" (long val) {
        when: "we check the value"
        OmniValue.checkValue(val)

        then: "no exception is thrown"
        true

        where:
        val << [0, 1, 9223372036854775807L]
    }

    @Unroll
    def "checkValue throws exception for invalid long value: #val" (long val) {
        when: "we check the value"
        OmniValue.checkValue(val)

        then: "NumberFormatException is thrown"
        NumberFormatException e = thrown()

        where:
        val << [-1, -2, -9223372036854775807L, -9223372036854775808L]
    }

    @Unroll
    def "checkValue does not throw exception for valid BigInteger value: #val" (BigInteger val) {
        when: "we check the value"
        OmniValue.checkValue(val)

        then: "no exception is thrown"
        true

        where:
        val << [0, 1, 9223372036854775807G]
    }

    @Unroll
    def "checkValue throws exception for invalid BigInteger value: #val" (BigInteger val) {
        when: "we check the value"
        OmniValue.checkValue(val)

        then: "NumberFormatException is thrown"
        NumberFormatException e = thrown()

        where:
        val << [-99999999999999999999999999G, -9223372036854775808G, -9223372036854775807G, -2, -1,
                9223372036854775808G, 99999999999999999999999999G]
    }
}
