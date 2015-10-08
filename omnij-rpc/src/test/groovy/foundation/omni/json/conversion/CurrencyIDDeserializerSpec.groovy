package foundation.omni.json.conversion

import com.fasterxml.jackson.databind.module.SimpleModule
import foundation.omni.CurrencyID
import foundation.omni.OmniValue
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import spock.lang.Specification
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
        '1'         | CurrencyID.MSC
        '2'         | CurrencyID.TMSC
        '3'         | CurrencyID.MaidSafeCoin
        '31'        | CurrencyID.TetherUS
    }

    @Override
    def configureModule(SimpleModule testModule) {
        testModule.addDeserializer(CurrencyID.class, new CurrencyIDDeserializer())
    }
}