package foundation.omni.money

import org.javamoney.moneta.CurrencyUnitBuilder
import org.javamoney.moneta.Money
import spock.lang.Shared
import spock.lang.Specification

import javax.money.CurrencyQuery
import javax.money.CurrencyQueryBuilder
import javax.money.CurrencyUnit
import javax.money.Monetary
import javax.money.MonetaryAmount


/**
 *
 */
class OmniCurrencyProviderSpec extends Specification {
    @Shared OmniCurrencyProvider provider

    def setup() {
        provider = new OmniCurrencyProvider();
    }

    def "can create new instance"() {
        expect:
        provider != null
    }

    def "returns set for empty query" () {
        when:
        CurrencyQuery query = CurrencyQueryBuilder.of().build()
        Set<CurrencyUnit> currencies = provider.getCurrencies(query)

        then:
        currencies.size() == 6
    }

    def "returns OMNI for OMNI query" () {
        when:
        CurrencyQuery query = CurrencyQueryBuilder.of().setCurrencyCodes("OMNI").build()
        Set<CurrencyUnit> currencies = provider.getCurrencies(query)
        CurrencyUnit unit = (CurrencyUnit) currencies.toArray()[0]

        then:
        currencies.size() == 1
        unit.getCurrencyCode() == "OMNI"
        unit.getDefaultFractionDigits() == 8
    }

    def "returns empty for USD query" () {
        when:
        CurrencyQuery query = CurrencyQueryBuilder.of().setCurrencyCodes("USD").build()
        Set<CurrencyUnit> currencies = provider.getCurrencies(query)

        then:
        currencies.size() == 0
    }

    def "OMNI can be found via Monetary (via META-INF.services)" () {
        when: "We try to get currency 'BTC' via registered services"
        CurrencyUnit unit = Monetary.getCurrency("OMNI")

        then: "we find it"
        unit.getCurrencyCode() == "OMNI"
        unit.getDefaultFractionDigits() == 8
    }

    def "MAID can be found via Monetary (via META-INF.services)" () {
        when: "We try to get currency 'BTC' via registered services"
        CurrencyUnit unit = Monetary.getCurrency("MAID")

        then: "we find it"
        unit.getCurrencyCode() == "MAID"
        unit.getDefaultFractionDigits() == 0
    }

}