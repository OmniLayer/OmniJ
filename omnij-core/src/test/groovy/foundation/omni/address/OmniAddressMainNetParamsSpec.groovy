package foundation.omni.address

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.Utils
import org.bitcoinj.params.MainNetParams
import spock.lang.Specification

/**
 * Proof-of-concept test of ability to generate Omni-specific addresses
 * via OmniAddressMainNetParams
 */
class OmniAddressMainNetParamsSpec extends Specification {
    static final omniParams = OmniAddressMainNetParams.get()
    static final btcParams = MainNetParams.get();

    def "can create an omni address"() {
        given: "A randomly generated ECKey"
        def key = new ECKey()

        when: "We generate an Omni address from it"
        def omniAddress = LegacyAddress.fromKey(omniParams, key)

        then: "It begins with an 'o'"
        omniAddress.toString().substring(0,1) == 'o'
    }

    def "can create an omni P2SH address"() {
        given: "An arbitrary hash value"
        byte[] hash160 = Utils.sha256hash160([0] as byte[])

        when: "We generate an Omni address from it"
        def omniAddress = LegacyAddress.fromScriptHash(omniParams, hash160)

        then: "It begins with an 'Q'"
        omniAddress.toString().substring(0,1) == 'Q'
    }
}
