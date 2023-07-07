package foundation.omni.net

import spock.lang.Specification

/**
 *
 */
class OmniNetworkSpec extends Specification {

    def "exodus"() {
        expect:
        OmniNetwork.MAINNET.exodusAddress() != null
    }
}
