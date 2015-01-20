package foundation.omni

import spock.lang.Specification

/**
 * User: sean
 * Date: 7/12/14
 * Time: 5:36 PM
 */
class EcosystemSpec extends Specification {

    def "Real MSC Ecosystem has value 1"() {
        when: "we create real ecosystem"
        Ecosystem ecosystem = new Ecosystem(1)

        then: "the value is 1"
        ecosystem == Ecosystem.MSC
        ecosystem.shortValue() == Ecosystem.MSC_VALUE
        ecosystem.byteValue() == (byte) 1
        ecosystem.shortValue() == (short) 1
        ecosystem.intValue() == 1
        ecosystem.longValue() == 1L
        ecosystem.floatValue() == 1.0F
        ecosystem.doubleValue() == 1.0D
    }

    def "Test MSC Ecosystem has value 2"() {
        when: "we create test ecosystem"
        Ecosystem ecosystem = new Ecosystem(2)

        then: "the value is 2"
        ecosystem == Ecosystem.TMSC
        ecosystem.shortValue() == Ecosystem.TMSC_VALUE
        ecosystem.byteValue() == (byte) 2
        ecosystem.shortValue() == (short) 2
        ecosystem.intValue() == 2
        ecosystem.longValue() == 2L
        ecosystem.floatValue() == 2.0F
        ecosystem.doubleValue() == 2.0D
    }

    def "constructor will take short or integer"() {
        when: "we create real ecosystem"
        Ecosystem e1 = new Ecosystem((short) 1)
        Ecosystem e2 = new Ecosystem(1)

        then: "they are the same"
        e1 == e2
    }

    def "constructor is strongly typed and won't allow all Number subclasses"() {
        when: "we try to create an ecosystem using an invalid numeric type"
        Ecosystem ecosystem = new Ecosystem(id)

        then: "exception is thrown"
        groovy.lang.GroovyRuntimeException e = thrown()

        where:
        id << [1F, 1.1F, 1.0D, 1.1D, 1.0, 1.1, 1.0G, 1.1G]
    }

    def "We can't create an Ecosystem with an invalid value"() {
        when: "we try to create an invalid ecosystem"
        Ecosystem ecosystem = new Ecosystem(id)

        then: "exception is thrown"
        NumberFormatException e = thrown()

        where:
        id << [0, -1, 3]
    }

    def "An Ecosystem can be represented as String"() {
        expect:
        Ecosystem ecosystem = new Ecosystem(id)
        ecosystem.toString() == ecosystemAsString

        where:
        id | ecosystemAsString
        1  | "Ecosystem:1"
        2  | "Ecosystem:2"
    }

}
