package foundation.omni.json.conversion

import com.fasterxml.jackson.databind.module.SimpleModule
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniIndivisibleValue
import foundation.omni.OmniValue
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
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

        when:
        def roundTrip = mapper.readValue(result, OmniValue.class)

        then:
        roundTrip == value

        where:
        expectedResult         | value
        '"21000000.0"'         | OmniDivisibleValue.ofWilletts(NetworkParameters.MAX_MONEY.value)
        '"1.0"'                | OmniDivisibleValue.ofWilletts(Coin.COIN.value)
        '"0.001"'              | OmniDivisibleValue.ofWilletts(Coin.MILLICOIN.value)
        '"0.000001"'           | OmniDivisibleValue.ofWilletts(Coin.MICROCOIN.value)
        '"0.00000001"'         | OmniDivisibleValue.ofWilletts(Coin.SATOSHI.value)
    }

    @Unroll
    def "fragment indivisible #value serializes as #expectedResult"() {
        when:
        def result = mapper.writeValueAsString(value)

        then:
        result == expectedResult

        when:
        def roundTrip = mapper.readValue(result, OmniValue.class)

        then:
        roundTrip == value

        where:
        expectedResult          | value
        '"2100000000000000"'    | OmniIndivisibleValue.ofWilletts(NetworkParameters.MAX_MONEY.value)
        '"100000000"'           | OmniIndivisibleValue.ofWilletts(Coin.COIN.value)
        '"100000"'              | OmniIndivisibleValue.ofWilletts(Coin.MILLICOIN.value)
        '"100"'                 | OmniIndivisibleValue.ofWilletts(Coin.MICROCOIN.value)
        '"1"'                   | OmniIndivisibleValue.ofWilletts(Coin.SATOSHI.value)
    }

    def configureModule(SimpleModule module) {
        module.addSerializer(OmniValue.class, new OmniValueSerializer())
        module.addDeserializer(OmniValue.class, new OmniValueDeserializer())
    }

}