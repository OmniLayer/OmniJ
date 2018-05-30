package foundation.omni.money

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 */
@Ignore("This is an integration test")
class KrakenXChangeRateProviderSpec extends Specification {
    @Shared BaseXChangeExchangeRateProvider provider

    def "can get an exchange rate (BTC/USD) via currency strings"() {
        when:
        def rate = provider.getExchangeRate("BTC", "USD")

        then:
        rate.factor.numberValue(BigDecimal.class) > 0
    }

    def "can get an exchange rate (USDT/USD) via currency strings"() {
        when:
        def rate = provider.getExchangeRate("USDT", "USD")

        then:
        rate.factor.numberValue(BigDecimal.class) > 0
    }

    def setup() {
        provider = new KrakenXChangeRateProvider()
    }

}
