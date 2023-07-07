package foundation.omni.address

import foundation.omni.net.OmniNetwork
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.base.SegwitAddress
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test conversion between BTC and Omni Segwit/Bech32 addresses
 */
class OmniSegwitAddressConverterSpec extends Specification {
    static final omniNetwork = OmniNetwork.MAINNET
    static final btcNetwork = BitcoinNetwork.MAINNET
    static final bitFinexColdWalletAddress = "bc1qgdjqv0av3q56jvd82tkdjpy7gdp9ut8tlqmgrpmv24sq90ecnvqqjwvw97"


    @Unroll
    def "A list of known address are convertible"(String btc, String omni) {
        expect: "Conversion works in both directions"
        omni == OmniSegwitAddressConverter.btcToOmni(fromBech32(btc)).toString()
        btc == OmniSegwitAddressConverter.omniToBtc(fromBech32(omni)).toString()

        where:
        btc                                                                 | omni
        'bc1qgdjqv0av3q56jvd82tkdjpy7gdp9ut8tlqmgrpmv24sq90ecnvqqjwvw97'    | 'o1qgdjqv0av3q56jvd82tkdjpy7gdp9ut8tlqmgrpmv24sq90ecnvqqu9f4ew'
        'bc1q5shngj24323nsrmxv99st02na6srekfctt30ch'                        | 'o1q5shngj24323nsrmxv99st02na6srekfc6rupyk'
        'bc1q2raxkmk55p000ggfa8euzs9fzq7p4cx4twycx7'                        | 'o1q2raxkmk55p000ggfa8euzs9fzq7p4cx46xfk6l'
        'bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4'                        | 'o1qw508d6qejxtdg4y5r3zarvary0c5xw7ka0ylh5'
        'tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx'                        | 'to1qw508d6qejxtdg4y5r3zarvary0c5xw7kjw58mu'
        'bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3'    | 'o1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qknvqsp'
        'tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7'    | 'to1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qz3y9y6'

    }

    def "2 way conversion works for a random address"() {
        given: "a random-generated Elliptic Curve private key and associated Bitcoin address"
        def btcAddress = new ECKey().toAddress(ScriptType.P2WPKH, btcNetwork)

        when: "we convert to Omni"
        SegwitAddress omniAddress = OmniSegwitAddressConverter.btcToOmni(btcAddress)

        then: "it's a valid Omni address"
        omniAddress.network() == omniNetwork
        omniAddress.getOutputScriptType() == ScriptType.P2WPKH
        omniAddress.toString().substring(0,2) == 'o1'

        when: "we convert back to Bitcoin"
        SegwitAddress backAgainBTCAddress = OmniSegwitAddressConverter.omniToBtc(omniAddress)

        then: "it's the same, valid BTC address"
        backAgainBTCAddress == btcAddress
        backAgainBTCAddress.network() == btcNetwork
        backAgainBTCAddress.getOutputScriptType() == ScriptType.P2WPKH
        backAgainBTCAddress.toString().substring(0,3) == 'bc1'
    }

    @Unroll
    def "2 way conversion works for Bitcoin address #addressString"(String addressString) {
        given: "a bitcoin address"
        def btcAddress = SegwitAddress.fromBech32(addressString, btcNetwork)

        when: "we convert to Omni"
        def omniAddress = OmniSegwitAddressConverter.btcToOmni(btcAddress)

        then: "it's a valid Omni address"
        omniAddress.network() == omniNetwork
        omniAddress.getOutputScriptType() == ScriptType.P2WPKH || omniAddress.getOutputScriptType() == ScriptType.P2WSH
        omniAddress.toString().substring(0,2) == 'o1'

        when: "we convert back to Bitcoin"
        def backAgainBTCAddress = OmniSegwitAddressConverter.omniToBtc(omniAddress)

        then: "it's the same, valid BTC address"
        backAgainBTCAddress == btcAddress
        backAgainBTCAddress.network() == btcNetwork
        backAgainBTCAddress.getOutputScriptType() == ScriptType.P2WPKH || backAgainBTCAddress.getOutputScriptType() == ScriptType.P2WSH

        where:
        addressString << [bitFinexColdWalletAddress, "bc1q5shngj24323nsrmxv99st02na6srekfctt30ch", "bc1q2raxkmk55p000ggfa8euzs9fzq7p4cx4twycx7"]
    }

    @Unroll
    def "2 way conversion works for Omni address #addressString"(String addressString) {
        given: "an Omni address"
        def omniAddress = OmniNetwork.addressParser.parseAddress(addressString, OmniNetwork.MAINNET)

        when: "we convert to BTC"
        def btcAddress = OmniSegwitAddressConverter.omniToBtc(omniAddress)

        then: "it's a valid BTC address"
        btcAddress.network() == btcNetwork
        btcAddress.getOutputScriptType() == ScriptType.P2WPKH || btcAddress.getOutputScriptType() == ScriptType.P2WSH
        btcAddress.toString().substring(0,3) == 'bc1'

        when: "we convert back to Omni"
        def backAgainOmniAddress = OmniSegwitAddressConverter.btcToOmni(btcAddress)

        then: "it's the same, valid Omni address"
        backAgainOmniAddress == omniAddress
        backAgainOmniAddress.network() == omniNetwork
        backAgainOmniAddress.getOutputScriptType() == ScriptType.P2WPKH || backAgainOmniAddress.getOutputScriptType() == ScriptType.P2WSH

        where:
        addressString << ["o1qgdjqv0av3q56jvd82tkdjpy7gdp9ut8tlqmgrpmv24sq90ecnvqqu9f4ew", "o1q5shngj24323nsrmxv99st02na6srekfc6rupyk"]
    }

    static SegwitAddress fromBech32(String addressString) {
        (SegwitAddress) OmniNetwork.addressParser.parseAddressAnyNetwork(addressString)
    }

    void setupSpec() {
        // Registration is not necessary here as long as OmniSegwitAddressConverter is calling bitcoinj's Networks.register
        // Networks.register([OmniAddressMainNetParams.get(), OmniAddressTestNetParams.get()])
    }
}
