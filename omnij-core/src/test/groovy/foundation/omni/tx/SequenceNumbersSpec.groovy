package foundation.omni.tx

import spock.lang.Specification

/**
 * Test SequenceNumbers
 *
 * @author msgilligan
 * @author dexX7
 */
class SequenceNumbersSpec extends BaseTxSpec {

    def "default sequence number is 0x01" () {
        expect:
        SequenceNumbers.add(hex("00")) == hex("0100")
    }

    def "insertion increases the size by 1 byte" () {
        expect:
        SequenceNumbers.add(hex("00"), (byte) 0x01).length == hex("00").length + 1
    }

    def "sequence number 0x02 is prepended" () {
        expect:
        SequenceNumbers.add(hex("0304050607"), (byte) 0x02) == hex("020304050607")
    }

    def "sequence number 0xff is prepended" () {
        expect:
        SequenceNumbers.add(hex("040815162342"), (byte) 0xff) == hex("ff040815162342")
    }

    def "one sequence number is prepended to a then whole packet" () {
        expect:
        SequenceNumbers.add(hex(    "000000000000000000000000000000000000000000000000000000000000")) ==
                            hex(    "01000000000000000000000000000000000000000000000000000000000000")
    }

    def "two sequence numbers are prepended" () {
        expect:
        SequenceNumbers.add(hex(    "000000000000000000000000000000000000000000000000000000000000" +
                                    "00")) ==
                            hex(    "01000000000000000000000000000000000000000000000000000000000000" +
                                    "0200")
    }

    def "sequence numbers 0x42 and 0x43 are prepended" () {
        expect:
        SequenceNumbers.add(hex(    "54686973206973206c6f6e676572207468616e206120726567756c617220" +
                                    "64617461207061636b6167652e"), (byte) 0x42) ==
                            hex(    "4254686973206973206c6f6e676572207468616e206120726567756c617220" +
                                    "4364617461207061636b6167652e")
    }

    def "input too long throws exception" () {
        when:
        def out =  SequenceNumbers.add(hex("000000000000000000000000000000000000000000000000000000000000" * Byte.MAX_VALUE + 1))

        then:
        IllegalArgumentException e = thrown()
    }

    def "starting seqnum too big throws exception" () {
        when:
        def out =  SequenceNumbers.add(hex("00"), Byte.MAX_VALUE + 1)

        then:
        IllegalArgumentException e = thrown()
    }
}