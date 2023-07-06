package foundation.omni.address

import foundation.omni.net.OmniNetwork
import org.bitcoinj.base.ScriptType
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.base.LegacyAddress
import org.bitcoinj.crypto.internal.CryptoUtils
import spock.lang.Specification

/**
 * Proof-of-concept test of ability to generate Omni-specific addresses
 * via OmniAddressMainNetParams
 */
class OmniAddressMainNetParamsSpec extends Specification {
    def "can create an omni address"() {
        given: "A randomly generated ECKey"
        def key = new ECKey()

        when: "We generate an Omni Base58 address from it"
        def omniAddress = key.toAddress(ScriptType.P2PKH, OmniNetwork.MAINNET)

        then: "It begins with a '1'"
        omniAddress.toString().substring(0,1) == '1'
    }

    def "can create an omni P2SH address"() {
        given: "An arbitrary hash value"
        byte[] hash160 = CryptoUtils.sha256hash160([0] as byte[])

        when: "We generate an Omni address from it"
        def omniAddress = LegacyAddress.fromScriptHash(OmniNetwork.MAINNET, hash160)

        then: "It begins with an '3'"
        omniAddress.toString().substring(0,1) == '3'
    }
}
