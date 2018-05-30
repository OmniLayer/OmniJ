package foundation.omni.dsl.categories

import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniIndivisibleValue
import spock.lang.Specification
import spock.util.mop.Use


/**
 * NumberCategory Spec
 */
@Use(NumberCategory)
class NumberCategorySpec extends Specification {

    def "basic test of .divisible convenience method"() {
        expect:
        1.divisible == OmniDivisibleValue.of(1)
        0.1G.divisible == OmniDivisibleValue.of(0.1)  // GROOVY-7608, Fixed in Groovy 2.5.0-alpha-1
    }

    def "basic test of .indivisible convenience method"() {
        expect:
        1.indivisible == OmniIndivisibleValue.of(1)
    }

    def "convert 0.1 to indivisible should throw ArithmeticException"() {
        given:
        def bd = new BigDecimal("0.1")

        when:
        bd.indivisible

        then:
        ArithmeticException e = thrown()
    }

}