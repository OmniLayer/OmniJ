package foundation.omni.json.conversion

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.bitcoinj.base.Coin
import spock.lang.Shared
import spock.lang.Specification


/**
 * Base class for testing serializers, deserializers in a mapper module
 */
abstract class BaseObjectMapperSpec extends Specification {
    @Shared
    ObjectMapper mapper

    def setup() {
        mapper = new ObjectMapper()
        def testModule = new SimpleModule("BitcoinJMappingClient", new Version(1, 0, 0, null, null, null))
        configureModule(testModule)
        mapper.registerModule(testModule)
    }

    /**
     * Override this class to configure your module
     * @param testModule
     */
    abstract configureModule(SimpleModule testModule)
}