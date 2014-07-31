package com.google.bitcoin.spock

import com.google.bitcoin.core.ECKey
import com.google.bitcoin.params.MainNetParams
import com.google.bitcoin.params.RegTestParams
import com.google.bitcoin.params.TestNet3Params
import spock.lang.Specification


class ECKeySpec extends Specification {
    static final mainNetParams = MainNetParams.get()
    static final testNetParams = TestNet3Params.get()
    static final regTestParams = RegTestParams.get()

    def "Generate a new, random valid Elliptic Curve Keypair"() {
        when: "We randomly generate a 256-bit private key and paired public key"
        def key = new ECKey()

        then: "it is a valid keypair"
        key.hasPrivKey()                    // Private key is present
        !key.pubKeyOnly                     // Yes, we have both. Really.
        key.privKeyBytes.length == 256/8    // is 256 bits (32 bytes) long
        key.encryptedPrivateKey == null     // This key has not been encrypted with a passphrase
        key.pubKey != null                  // Raw public key value as appears in scriptSigs
        // pubKey is 33 bytes when compressed, otherwise 66 bytes
        (key.pubKey.length == 33 && key.compressed) ||
        (key.pubKey.length == 65 && !key.compressed)
        key.pubKeyCanonical                 // Canonical makes sure length is right for compressed/uncompressed
        key.pubKeyHash.length == 20         // Is available in RIPEMD160 form
        // Can be converted to addresses (which have a different header for each network
        key.toAddress(mainNetParams).version == mainNetParams.addressHeader
        key.toAddress(testNetParams).version == testNetParams.addressHeader
        key.toAddress(regTestParams).version == regTestParams.addressHeader
        key.creationTimeSeconds > 0          // since we created it, we know the creation time
    }

    def "Import a constant, publicly-known private key "() {
        // TBD
    }

    def "Use a key for signing and verifying messages"() {
        // TBD
    }
}