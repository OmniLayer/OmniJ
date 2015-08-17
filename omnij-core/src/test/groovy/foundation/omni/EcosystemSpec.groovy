package foundation.omni

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Specification for Omni Ecosystem class
 */
class EcosystemSpec extends Specification {

    def "Real MSC Ecosystem has value 1"() {
        when: "we create real ecosystem"
        Ecosystem ecosystem = Ecosystem.MSC

        then: "the value is 1"
        ecosystem == Ecosystem.MSC
        ecosystem.getValue() == Ecosystem.MSC_VALUE
        ecosystem.getValue() == (short) 1
    }

    def "Test MSC Ecosystem has value 2"() {
        when: "we create test ecosystem"
        Ecosystem ecosystem = Ecosystem.TMSC

        then: "the value is 2"
        ecosystem == Ecosystem.TMSC
        ecosystem.getValue() == Ecosystem.TMSC_VALUE
        ecosystem.getValue() == (short) 2
    }

    @Unroll
    def "constructor is strongly typed and won't allow all Number subclasses (#id)"() {
        when: "we try to create an ecosystem using an invalid numeric type"
        Ecosystem ecosystem = new Ecosystem(id)

        then: "exception is thrown"
        groovy.lang.GroovyRuntimeException e = thrown()

        where:
        id << [1F, 1.1F, 1.0D, 1.1D, 1.0, 1.1, 1.0G, 1.1G]
    }

    @Unroll
    def "An Ecosystem can be represented as String (#id -> #ecosystemAsString)"() {
        expect:
        ecosystem.toString() == ecosystemAsString

        where:
        ecosystem         | ecosystemAsString
        Ecosystem.MSC     | "Ecosystem:1"
        Ecosystem.TMSC    | "Ecosystem:2"
    }

}
