package foundation.omni.json.conversion

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.msgilligan.bitcoinj.json.conversion.RpcClientModule
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.RegTestParams
import spock.lang.Shared
import spock.lang.Specification


/**
 *
 */
abstract class BaseOmniClientMapperSpec extends Specification {
    @Shared
    def mapper

    def setup() {
        mapper = new ObjectMapper()
        mapper.registerModule(new RpcClientModule(RegTestParams.get()))
        mapper.registerModule(new OmniClientModule(RegTestParams.get()))
    }


}