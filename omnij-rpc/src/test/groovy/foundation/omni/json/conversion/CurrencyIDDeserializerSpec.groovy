package foundation.omni.json.conversion

import com.fasterxml.jackson.databind.module.SimpleModule
import foundation.omni.CurrencyID
import spock.lang.Unroll


/**
 *
 */
class CurrencyIDDeserializerSpec extends BaseObjectMapperSpec {

    @Unroll
    def "fragment #fragment scans to #expectedResult"() {
        when:
        def result = mapper.readValue(fragment, CurrencyID.class)

        then:
        result == expectedResult

        where:
        fragment    | expectedResult
        '0'         | CurrencyID.BTC
        '1'         | CurrencyID.OMNI
        '2'         | CurrencyID.TOMNI
        '3'         | CurrencyID.MAID
        '31'        | CurrencyID.USDT
    }

    @Override
    def configureModule(SimpleModule testModule) {
        testModule.addDeserializer(CurrencyID.class, new CurrencyIDDeserializer())
    }
}