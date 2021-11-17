package foundation.omni

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Specification for Omni Ecosystem class
 */
class EcosystemSpec extends Specification {

    def "Real Omni Ecosystem has value 1"() {
        when: "we create real ecosystem"
        Ecosystem ecosystem = Ecosystem.OMNI

        then: "the value is 1"
        ecosystem == Ecosystem.OMNI
        ecosystem.name() == "OMNI"
        ecosystem.value() == (short) 1
    }

    def "Test Omni Ecosystem has value 2"() {
        when: "we create test ecosystem"
        Ecosystem ecosystem = Ecosystem.TOMNI

        then: "the value is 2"
        ecosystem == Ecosystem.TOMNI
        ecosystem.name() == "TOMNI"
        ecosystem.value() == (short) 2
    }

    @Unroll
    def "An Ecosystem can be represented as String (#ecosystem -> #ecosystemAsString)"(Ecosystem ecosystem, String ecosystemAsString) {
        expect:
        ecosystem.toString() == ecosystemAsString

        where:
        ecosystem       | ecosystemAsString
        Ecosystem.OMNI  | "OMNI"
        Ecosystem.TOMNI | "TOMNI"
    }

}
