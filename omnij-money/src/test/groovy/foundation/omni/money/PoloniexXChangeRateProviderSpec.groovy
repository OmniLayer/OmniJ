package foundation.omni.money

import com.msgilligan.bitcoinj.money.BaseXChangeExchangeRateProvider
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification


/**
 *  Quick integration test of PoloniexXChangeRateProvide
 */
@Ignore("this is really an integration test")
class PoloniexXChangeRateProviderSpec extends Specification {
    @Shared BaseXChangeExchangeRateProvider provider

    def "can get an exchange rate via currency strings"() {
        when:
        def rate = provider.getExchangeRate("OMNI", "BTC")

        then:
        rate.factor.numberValue(BigDecimal.class) > 0
    }

    def "can list currency codes"() {
        when:
        def markets = provider.exchange.metaData.marketMetaDataMap

        then:
        markets.size() > 0
    }

    def setup() {
        provider = new PoloniexXChangeRateProvider()
    }

}