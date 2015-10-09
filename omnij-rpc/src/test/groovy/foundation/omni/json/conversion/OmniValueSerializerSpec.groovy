package foundation.omni.json.conversion

import com.fasterxml.jackson.databind.module.SimpleModule
import com.msgilligan.bitcoinj.json.conversion.CoinSerializer
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniIndivisibleValue
import foundation.omni.OmniValue
import foundation.omni.PropertyType
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import spock.lang.Specification
import spock.lang.Unroll


/**
 *
 */
class OmniValueSerializerSpec extends BaseObjectMapperSpec {
    @Unroll
    def "fragment divisble #value serializes as #expectedResult"() {
        when:
        def result = mapper.writeValueAsString(value)

        then:
        result == expectedResult

        where:
        expectedResult         | value
        '"21000000"'           | OmniDivisibleValue.ofWillets(NetworkParameters.MAX_MONEY.value)
        '"1"'                  | OmniDivisibleValue.ofWillets(Coin.COIN.value)
        '"0.001"'              | OmniDivisibleValue.ofWillets(Coin.MILLICOIN.value)
        '"0.000001"'           | OmniDivisibleValue.ofWillets(Coin.MICROCOIN.value)
        '"0.00000001"'         | OmniDivisibleValue.ofWillets(Coin.SATOSHI.value)
    }

    @Unroll
    def "fragment indivisible #value serializes as #expectedResult"() {
        when:
        def result = mapper.writeValueAsString(value)

        then:
        result == expectedResult

        where:
        expectedResult          | value
        '"2100000000000000"'    | OmniIndivisibleValue.ofWillets(NetworkParameters.MAX_MONEY.value)
        '"100000000"'           | OmniIndivisibleValue.ofWillets(Coin.COIN.value)
        '"100000"'              | OmniIndivisibleValue.ofWillets(Coin.MILLICOIN.value)
        '"100"'                 | OmniIndivisibleValue.ofWillets(Coin.MICROCOIN.value)
        '"1"'                   | OmniIndivisibleValue.ofWillets(Coin.SATOSHI.value)
    }

    def configureModule(SimpleModule module) {
        module.addSerializer(OmniValue.class, new OmniValueSerializer())
    }

}