package foundation.omni.address

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.Script
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Proof-of-concept test of conversion between Omni and BTC addresses
 */
class OmniAddressConverterSpec extends Specification {
    static final omniParams = OmniAddressMainNetParams.get()
    static final btcParams = MainNetParams.get();

    def "2 way conversion works for a random address"() {
        given: "a random bitcoin address"
        def key = new ECKey()
        def btcAddress = LegacyAddress.fromKey(btcParams, key)

        when: "we convert to Omni"
        LegacyAddress omniAddress = OmniAddressConverter.btcToOmni(btcAddress)

        then: "it's a valid Omni address"
        omniAddress.parameters == omniParams
        omniAddress.getOutputScriptType() == Script.ScriptType.P2PKH
        omniAddress.toString().substring(0,1) == 'o'

        when: "we convert back to Bitcoin"
        LegacyAddress backAgainBTCAddress = OmniAddressConverter.omniToBTC(omniAddress)

        then: "it's the same, valid BTC address"
        backAgainBTCAddress == btcAddress
        backAgainBTCAddress.parameters == btcParams
        backAgainBTCAddress.getOutputScriptType() == Script.ScriptType.P2PKH
        backAgainBTCAddress.toString().substring(0,1) == '1'
    }

    @Unroll
    def "2 way conversion works for Bitcoin address #addressString"(String addressString) {
        given: "a bitcoin address"
        def btcAddress = Address.fromString(btcParams, addressString)

        when: "we convert to Omni"
        def omniAddress = OmniAddressConverter.btcToOmni(btcAddress)

        then: "it's a valid Omni address"
        omniAddress.parameters == omniParams
        omniAddress.getOutputScriptType() == Script.ScriptType.P2PKH
        omniAddress.toString().substring(0,1) == 'o'

        when: "we convert back to Bitcoin"
        def backAgainBTCAddress = OmniAddressConverter.omniToBTC(omniAddress)

        then: "it's the same, valid BTC address"
        backAgainBTCAddress == btcAddress
        backAgainBTCAddress.parameters == btcParams
        backAgainBTCAddress.getOutputScriptType() == Script.ScriptType.P2PKH
        backAgainBTCAddress.toString().substring(0,1) == '1'

        where:
        addressString << ["1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P", "1Po1oWkD2LmodfkBYiAktwh76vkF93LKnh", "19w61aha78sXnqvVZqRYZPzK5R6WE8rUmT"]
    }

    @Unroll
    def "2 way conversion works for Omni address #addressString"(String addressString) {
        given: "an Omni address"
        def omniAddress = Address.fromString(omniParams, addressString)

        when: "we convert to BTC"
        def btcAddress = OmniAddressConverter.omniToBTC(omniAddress)

        then: "it's a valid BTC address"
        btcAddress.parameters == btcParams
        btcAddress.getOutputScriptType() == Script.ScriptType.P2PKH
        btcAddress.toString().substring(0,1) == '1'

        when: "we convert back to Omni"
        def backAgainOmniAddress = OmniAddressConverter.btcToOmni(btcAddress)

        then: "it's the same, valid Omni address"
        backAgainOmniAddress == omniAddress
        backAgainOmniAddress.parameters == omniParams
        backAgainOmniAddress.getOutputScriptType() == Script.ScriptType.P2PKH
        backAgainOmniAddress.toString().substring(0,1) == 'o'

        where:
        addressString << ["oWQATQ9rvhDYnQWw91GCptai37kTq2rRQr", "offP312Lg64ZgWn9MxUQfMzaUwNitETkBp", "oRoTF4yhktAHqgxTP5jCKpHnTRiz2134jk"]
    }

}
