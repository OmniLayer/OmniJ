package foundation.omni.tx

import spock.lang.Specification


/**
 *
 */
class PubKeyConversionSpec extends Specification {

    def "Can convert one string to a key" () {
        when:
        def input = hex("777ac9576cb08fb869efd4be2ca094c55808054e2220285f3c754d59361b30")
        def key = PubKeyConversion.createPubKey(input)

        then:
        key.pubKey == hex("03777ac9576cb08fb869efd4be2ca094c55808054e2220285f3c754d59361b3000")

    }

    def "Can convert a string to a key list" () {
        when:
        def list = PubKeyConversion.convert(hex("12345600000000000000000000000000000000000000000000000000000000"));

        then:
        list.size() == 1
        list[0].pubKey == hex("031234560000000000000000000000000000000000000000000000000000000001");
    }

    byte[] hex(String string) {
        return string.decodeHex()
    }
}