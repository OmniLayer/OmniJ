package com.google.bitcoin.spock

import com.google.bitcoin.core.ECKey
import org.spongycastle.util.encoders.Hex
import spock.lang.Specification

class AddressSpec extends Specification {
    final static BigInteger NotSoPrivatePrivateKey = new BigInteger(1, Hex.decode("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19"));

    def "Import private key to MainNet"() {
        setup: ""
        def key = new ECKey(NotSoPrivatePrivateKey)
    }
}