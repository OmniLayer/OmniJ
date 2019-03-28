package foundation.omni.json.conversion

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule
import foundation.omni.OmniDivisibleValue
import foundation.omni.OmniIndivisibleValue
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.RegTestParams
import org.bitcoinj.params.TestNet3Params
import spock.lang.Shared
import spock.lang.Specification


/**
 *
 */
abstract class BaseOmniClientMapperSpec extends Specification {
    static final OmniDivisibleValue d0 = 0.divisible
    static final OmniDivisibleValue d1 = 1.divisible
    static final OmniIndivisibleValue i0 = 0.indivisible
    static final OmniIndivisibleValue i1 = 1.indivisible

    @Shared
    def mapper

    def setupSpec() {
        NetworkParameters netParams = TestNet3Params.get()
        mapper = new ObjectMapper()
        mapper.registerModule(new RpcClientModule(netParams))
        mapper.registerModule(new OmniClientModule(netParams))
    }


}