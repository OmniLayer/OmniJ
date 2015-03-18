package org.bitcoinj.spock

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.params.TestNet3Params
import org.spongycastle.util.encoders.Hex
import spock.lang.Ignore
import spock.lang.Specification

class AddressSpec extends Specification {
    static final mainNetParams = MainNetParams.get()
    static final testNetParams = TestNet3Params.get()
    static final regTestParams = RegTestParams.get()
    final static BigInteger NotSoPrivatePrivateKey = new BigInteger(1, Hex.decode("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));

    def "Create valid MainNet Address from private key"() {
        setup: ""
        def key = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)

        when: "We create a MainNet Address"
        Address address = key.toAddress(mainNetParams)

        then: "It has expected value and properties"
        address.toString() == "1GtCqbyqTzbvtBWMMRgkwkxenPJNzz1TY4"
        address.version == mainNetParams.addressHeader
        address.parameters == mainNetParams
    }

    def "Create valid TestNet Address from private key"() {
        setup: ""
        def key = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)

        when: "We create a TestNet Address"
        Address address = key.toAddress(testNetParams)

        then: "It has expected value and properties"
        address.toString() == "mwQA8f4pH23BfHyy4zf8mgAyeNu5uoy6GU"
        address.version == testNetParams.addressHeader
        address.parameters == testNetParams
    }

    def "Create valid RegTest Address from private key"() {
        setup: ""
        def key = ECKey.fromPrivate(NotSoPrivatePrivateKey, false)

        when: "We create a RegTest Address"
        Address address = key.toAddress(regTestParams)

        then: "It has expected value and properties"
        address.toString() == "mwQA8f4pH23BfHyy4zf8mgAyeNu5uoy6GU"
        address.version == regTestParams.addressHeader
        address.parameters == regTestParams
    }

}