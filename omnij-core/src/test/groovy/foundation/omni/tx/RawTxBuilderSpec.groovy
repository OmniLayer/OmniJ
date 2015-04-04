package foundation.omni.tx

import org.bitcoinj.core.Coin
import spock.lang.Shared
import spock.lang.Specification

import static foundation.omni.CurrencyID.MSC


/**
 * RawTxBuilder unit tests
 */
class RawTxBuilderSpec extends Specification {
    @Shared
    RawTxBuilder builder

    def setup() {
        builder = new RawTxBuilder()
    }

    def "The generated hex-encoded Simple Send transaction matches a valid reference transaction"() {
        when:
        def txHex = builder.createSimpleSendHex(MSC,
                Coin.COIN_VALUE)

        then:
        txHex == "00000000000000010000000005f5e100"
    }

    def "The generated hex-encoded Simple To Owners transaction matches a valid reference transaction"() {
        when:
        def txHex = builder.createSendToOwnersHex(MSC,
                Coin.COIN_VALUE)

        then:
        txHex == "00000003000000010000000005f5e100"
    }

    def "The generated hex-encoded Dex Sell Offer transaction matches a valid reference transaction"() {
        when:
        def txHex = builder.createDexSellOfferHex(MSC,
                Coin.COIN_VALUE,    // amount for sale: 1 BTC in satoshis
                20000000,           // amount desired: 0.2 BTC in satoshis
                (Byte) 10,          // payment window in blocks
                10000,              // commitment Fee in satoshis
                (Byte) 1)           // sub-action: new offer

        then:
        txHex == "00010014000000010000000005f5e1000000000001312d000a000000000000271001"
    }
}