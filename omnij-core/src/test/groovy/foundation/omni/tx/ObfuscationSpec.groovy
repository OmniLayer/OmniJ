package foundation.omni.tx

import spock.lang.Specification


/**
 * Test Obfuscation
 *
 * @author msgilligan
 * @author dexX7
 */
class ObfuscationSpec extends Specification {

    def "upper hash works"() {
        given:
        def seed = "1CdighsfdfRcj4ytQSskZgQXbUEamuMUNF"
        def hash = "1D9A3DE5C2E22BF89A1E41E6FEDAB54582F8A0C3AE14394A59366293DD130C59"

        when:
        def h1 = Obfuscation.upperSha256(seed)

        then:
        h1 == hash

        when:
        def h2 = Obfuscation.upperSha256(h1)

        then:
        h2 == "0800ED44F1300FB3A5980ECFA8924FEDB2D5FDBEF8B21BBA6526B4FD5F9C167C"

        when:
        def h3 = Obfuscation.upperSha256(h2)

        then:
        h3 == "7110A59D22D5AF6A34B7A196DAE7CCC0F27354B34E257832B9955611A9D79B06"

    }

    def "obfuscation is reversible"() {
        given:
        def msg = hex("0100000000000000010000000002faf0800000000000000000000000000000")
        def seed = "1CdighsfdfRcj4ytQSskZgQXbUEamuMUNF"

        when:
        def enc1 = Obfuscation.obfuscate(msg, seed)
        def enc2 = Obfuscation.obfuscate(enc1, seed)

        then:
        enc1.length == msg.length
        enc2.length == enc1.length
        enc2 == msg
    }

    byte[] hex(String string) {
        return string.decodeHex()
    }

}