package foundation.omni.consensus

import spock.lang.Specification


/**
 * ConsensusEntryPair Tests
 */
class ConsensusEntryPairSpec extends Specification {

    def "is immutable"() {
        setup:
        def pair = new ConsensusEntryPair("address", new ConsensusEntry(0,0), new ConsensusEntry(0,0))

        when: "we try to change it"
        pair.entry1 = new ConsensusEntry(1,1)

        then: "an exception is thrown and value isn't changed"
        ReadOnlyPropertyException e = thrown()
    }

    def "is iterable"() {
        setup:
        def pair = new ConsensusEntryPair("address", new ConsensusEntry(0,0), new ConsensusEntry(1,1))

        when: "we iterate it"
        def objs = []
        for (item in pair) {
            objs.add(item)
        }

        then: "it worked"
        objs.size() == 3
        objs[0] == "address"
        objs[1] == new ConsensusEntry(0,0)
        objs[2] == new ConsensusEntry(1,1)
    }

}