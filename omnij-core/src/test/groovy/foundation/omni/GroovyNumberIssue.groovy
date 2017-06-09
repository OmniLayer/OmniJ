package foundation.omni

import spock.lang.Specification


/**
 * Tests that illustrate a (NOW FIXED) Groovy Bug
 * See https://issues.apache.org/jira/browse/GROOVY-7608
 */
class GroovyNumberIssue extends Specification {
    
    // Works in Groovy 2.4.x and Groovy 2.5
    def "Equality works for integer values"() {
        expect:
        OmniDivisibleValue.of(1) == OmniDivisibleValue.of(1)
    }

    // Was broken in Groovy 2.4.x, fixed in 2.5.0-alpha-1 and later
    def "Equality works for decimal values"() {
        expect:
        OmniDivisibleValue.of(1.1) == OmniDivisibleValue.of(1.1)
    }

    // Was broken in Groovy 2.4.x, fixed in 2.5.0-alpha-1 and later
    def "Inequality works for integer/decimal values"() {
        expect:
        OmniDivisibleValue.of(1) != OmniDivisibleValue.of(1.1)
    }

}