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
        ecosystem.getValue() == Ecosystem.OMNI_VALUE
        ecosystem.getValue() == (short) 1
    }

    def "Test Omni Ecosystem has value 2"() {
        when: "we create test ecosystem"
        Ecosystem ecosystem = Ecosystem.TOMNI

        then: "the value is 2"
        ecosystem == Ecosystem.TOMNI
        ecosystem.getValue() == Ecosystem.TOMNI_VALUE
        ecosystem.getValue() == (short) 2
    }

    @Unroll
    def "constructor is strongly typed and won't allow all Number subclasses (#id)"(id) {
        when: "we try to create an ecosystem using an invalid numeric type"
        Ecosystem ecosystem = new Ecosystem(id)

        then: "exception is thrown"
        GroovyRuntimeException e = thrown()

        where:
        id << [1F, 1.1F, 1.0D, 1.1D, 1.0, 1.1, 1.0G, 1.1G]
    }

    @Unroll
    def "An Ecosystem can be represented as String (#ecosystem -> #ecosystemAsString)"(Ecosystem ecosystem, String ecosystemAsString) {
        expect:
        ecosystem.toString() == ecosystemAsString

        where:
        ecosystem       | ecosystemAsString
        Ecosystem.OMNI  | "Ecosystem:1"
        Ecosystem.TOMNI | "Ecosystem:2"
    }

}
