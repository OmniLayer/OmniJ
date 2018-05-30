package foundation.omni.address

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.MainNetParams
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
        def btcAddress = key.toAddress(btcParams)

        when: "we convert to Omni"
        def omniAddress = OmniAddressConverter.btcToOmni(btcAddress)

        then: "it's a valid Omni address"
        omniAddress.parameters == omniParams
        !omniAddress.isP2SHAddress()
        omniAddress.toString().substring(0,1) == 'o'

        when: "we convert back to Bitcoin"
        def backAgainBTCAddress = OmniAddressConverter.omniToBTC(omniAddress)

        then: "it's the same, valid BTC address"
        backAgainBTCAddress == btcAddress
        backAgainBTCAddress.parameters == btcParams
        !backAgainBTCAddress.isP2SHAddress()
        backAgainBTCAddress.toString().substring(0,1) == '1'
    }

    @Unroll
    def "2 way conversion works for Bitcoin address #addressString"(String addressString) {
        given: "a bitcoin address"
        def btcAddress = new Address(btcParams, addressString)

        when: "we convert to Omni"
        def omniAddress = OmniAddressConverter.btcToOmni(btcAddress)
        println "btc address -> omni address: ${btcAddress} -> ${omniAddress}"

        then: "it's a valid Omni address"
        omniAddress.parameters == omniParams
        !omniAddress.isP2SHAddress()
        omniAddress.toString().substring(0,1) == 'o'

        when: "we convert back to Bitcoin"
        def backAgainBTCAddress = OmniAddressConverter.omniToBTC(omniAddress)

        then: "it's the same, valid BTC address"
        backAgainBTCAddress == btcAddress
        backAgainBTCAddress.parameters == btcParams
        !backAgainBTCAddress.isP2SHAddress()
        backAgainBTCAddress.toString().substring(0,1) == '1'

        where:
        addressString << ["1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P", "1Po1oWkD2LmodfkBYiAktwh76vkF93LKnh", "19w61aha78sXnqvVZqRYZPzK5R6WE8rUmT"]
    }

    @Unroll
    def "2 way conversion works for Omni address #addressString"(String addressString) {
        given: "an Omni address"
        def omniAddress = new Address(omniParams, addressString)

        when: "we convert to BTC"
        def btcAddress = OmniAddressConverter.omniToBTC(omniAddress)
        println "omni address -> btc address: ${omniAddress} -> ${btcAddress} "

        then: "it's a valid BTC address"
        btcAddress.parameters == btcParams
        !btcAddress.isP2SHAddress()
        btcAddress.toString().substring(0,1) == '1'

        when: "we convert back to Omni"
        def backAgainOmniAddress = OmniAddressConverter.btcToOmni(btcAddress)

        then: "it's the same, valid Omni address"
        backAgainOmniAddress == omniAddress
        backAgainOmniAddress.parameters == omniParams
        !backAgainOmniAddress.isP2SHAddress()
        backAgainOmniAddress.toString().substring(0,1) == 'o'

        where:
        addressString << ["oWQATQ9rvhDYnQWw91GCptai37kTq2rRQr", "offP312Lg64ZgWn9MxUQfMzaUwNitETkBp", "oRoTF4yhktAHqgxTP5jCKpHnTRiz2134jk"]
    }

}
