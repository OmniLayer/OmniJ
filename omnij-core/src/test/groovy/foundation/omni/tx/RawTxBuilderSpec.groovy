package foundation.omni.tx

import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniIndivisibleValue
import foundation.omni.PropertyType
import spock.lang.Shared
import spock.lang.Specification

/**
 * RawTxBuilder unit tests
 */
class RawTxBuilderSpec extends Specification {
    final static OmniDivisibleValue ONE_OMNI = OmniDivisibleValue.of(1)

    @Shared
    RawTxBuilder builder

    def setup() {
        builder = new RawTxBuilder()
    }

    def "The hex-encoded raw transaction matches valid reference: simple send [type 0, version 0]"() {
        when:
        def txHex = builder.createSimpleSendHex(
                CurrencyID.OMNI,   // property
                ONE_OMNI)         // amount to transfer: 1.0 OMNI (in willetts)

        then:
        txHex == "00000000000000010000000005f5e100"
    }

    def "The hex-encoded raw transaction matches valid reference: send to owners [type 3, version 0]"() {
        when:
        def txHex = builder.createSendToOwnersHex(
                CurrencyID.OMNI,   // property
                ONE_OMNI)         // amount to distribute: 1.0 OMNI (in willetts)

        then:
        txHex == "00000003000000010000000005f5e100"
    }

    def "The hex-encoded raw transaction matches valid reference: sell tokens for bitcoins [type 20, version 1]"() {
        when:
        def txHex = builder.createDexSellOfferHex(
                CurrencyID.OMNI,   // property
                ONE_OMNI,         // amount for sale: 1.0 OMNI (in willetts)
                0.2.btc,         // amount desired: 0.2 BTC (in satoshis)
                (Byte) 10,        // payment window in blocks
                0.0001.btc,       // commitment fee in satoshis
                (Byte) 1)         // sub-action: new offer

        then:
        txHex == "00010014000000010000000005f5e1000000000001312d000a000000000000271001"
    }

    def "The hex-encoded raw transaction matches valid reference: trade tokens for tokens [type 21, version 0]"() {
        when:
        def txHex = builder.createMetaDexSellOfferHex(
                CurrencyID.OMNI,       // property
                OmniDivisibleValue.of(2.5),            // amount for sale: 2.5 OMNI
                CurrencyID.USDT,  // property desired
                OmniDivisibleValue.of(50),          // amount desired: 50.0 TetherUS
                (Byte) 1)             // sub-action: new offer

        then:
        txHex == "0000001500000001000000000ee6b2800000001f000000012a05f20001"
    }

    def "The hex-encoded raw transaction matches valid reference: purchase tokens with bitcoins [type 22, version 0]"() {
        when:
        def txHex = builder.createAcceptDexOfferHex(
                CurrencyID.OMNI,  // property
                OmniDivisibleValue.of(1.3))       // amount to purchase: 1.3 OMNI

        then:
        txHex == "00000016000000010000000007bfa480"
    }

    def "The hex-encoded raw transaction matches valid reference: create property [type 50, version 0]"() {
        when:
        def txHex = builder.createPropertyHex(
                Ecosystem.OMNI,             // ecosystem: main
                PropertyType.INDIVISIBLE,  // property type: indivisible tokens
                0,                         // previous property: none
                "Companies",               // category
                "Bitcoin Mining",          // subcategory
                "Quantum Miner",           // label
                "builder.bitwatch.co",     // website
                "",                        // additional information
                OmniIndivisibleValue.of(1000000))                   // number of units to create

        then:
        txHex == "0000003201000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74" +
                 "756d204d696e6572006275696c6465722e62697477617463682e636f000000000000000f4240"
    }

    def "The hex-encoded raw transaction matches valid reference: create crowdsale [type 51, version 0]"() {
        when:
        def txHex = builder.createCrowdsaleHex(
                Ecosystem.OMNI,             // ecosystem: main
                PropertyType.INDIVISIBLE,  // property type: indivisible tokens
                0,                         // previous property: none
                "Companies",               // category
                "Bitcoin Mining",          // subcategory
                "Quantum Miner",           // label
                "builder.bitwatch.co",     // website
                "",                        // additional information
                CurrencyID.OMNI,            // property desired
                100,                       // tokens per unit vested
                7731414000L,               // deadline: 31 Dec 2214 23:00:00 UTC
                (Byte) 10,                 // early bird bonus: 10 % per week
                (Byte) 12)                 // issuer bonus: 12 %

        then:
        txHex == "0000003301000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74" +
                 "756d204d696e6572006275696c6465722e62697477617463682e636f00000000000100000000000000" +
                 "6400000001ccd403f00a0c"
    }

    def "The hex-encoded raw transaction matches valid reference: close crowdsale [type 53, version 0]"() {
        when:
        def txHex = builder.createCloseCrowdsaleHex(new CurrencyID(9))

        then:
        txHex == "0000003500000009"
    }

    def "The hex-encoded raw transaction matches valid reference: create managed property [type 54, version 0]"() {
        when:
        def txHex = builder.createManagedPropertyHex(
                Ecosystem.OMNI,             // ecosystem: main
                PropertyType.INDIVISIBLE,  // property type: indivisible tokens
                0,                         // previous property: none
                "Companies",               // category
                "Bitcoin Mining",          // subcategory
                "Quantum Miner",           // label
                "builder.bitwatch.co",     // website
                "")                        // additional information

        then:
        txHex == "0000003601000100000000436f6d70616e69657300426974636f696e204d696e696e67005175616e74" +
                 "756d204d696e6572006275696c6465722e62697477617463682e636f0000"
    }

    def "The hex-encoded raw transaction matches valid reference: grant tokens [type 55, version 0]"() {
        when:
        def txHex = builder.createGrantTokensHex(
                new CurrencyID(8),           // property
                OmniDivisibleValue.ofWilletts(1000),                        // number of units to issue
                "First Milestone Reached!")  // additional information

        then:
        txHex == "000000370000000800000000000003e84669727374204d696c6573746f6e6520526561636865642100"
    }

    def "The hex-encoded raw transaction matches valid reference: revoke tokens [type 56, version 0]"() {
        when:
        def txHex = builder.createRevokeTokensHex(
                new CurrencyID(8),                            // property
                OmniDivisibleValue.ofWilletts(1000),                                         // number of units to revoke
                "Redemption of tokens for Bob, Thanks Bob!")  // additional information

        then:
        txHex == "000000380000000800000000000003e8526564656d7074696f6e206f6620746f6b656e7320666f7220" +
                 "426f622c205468616e6b7320426f622100"
    }

    def "The hex-encoded raw transaction matches valid reference: change property manager [type 70, version 0]"() {
        when:
        def txHex = builder.createChangePropertyManagerHex(new CurrencyID(13))

        then:
        txHex == "000000460000000d"
    }

}
