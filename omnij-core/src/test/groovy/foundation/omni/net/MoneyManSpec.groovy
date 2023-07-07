package foundation.omni.net

import foundation.omni.OmniDivisibleValue
import org.bitcoinj.base.Coin
import spock.lang.Specification
import spock.lang.Unroll

/**
 *  Test spec for MoneyMan conversions
 */
class MoneyManSpec extends Specification {
    @Unroll
    def "Convert #satoshis -> #willets" (long satoshis, long willets) {
        when: "we try to create an OmniValue using a valid numeric type"
        OmniDivisibleValue value = MoneyMan.toOmni(Coin.ofSat(satoshis))

        then: "it is created correctly"
        value.getWilletts() == willets

        where:
        satoshis                    | willets
        0                           | 0
        1                           | 100
        100                         | 10000
        Coin.FIFTY_COINS.toSat()    | 500_000_000_000
    }

    @Unroll
    def "Calculate required Bitcoin #willets -> #satoshis" (long willets, long satoshis) {
        when: "we try to create an OmniValue using a valid numeric type"
        Coin value = MoneyMan.requiredBitcoin(OmniDivisibleValue.ofWilletts(willets))

        then: "it is created correctly"
        value.toSat() == satoshis

        where:
        willets                     | satoshis
        0                           | 0
        1                           | 1
        99                          | 1
        100                         | 1
        101                         | 2
        200                         | 2
        500_000_000_000             | Coin.FIFTY_COINS.toSat()
    }
}
