package foundation.omni.rest.omniwallet

import foundation.omni.rest.omniwallet.json.OmniwalletPropertiesListResponse
import spock.lang.Specification

/**
 *  Test loading PropertyListInfo from a file
 */
class PropertyListInfoFileSpec extends Specification {
    def "load test file resource"() {
        given:
        InputStream is =  PropertyListInfoFileSpec.getResourceAsStream("/foundation/omni/rest/omniwallet/omniwallet_property_list_w_blockheight.json")
        
        when:
        OmniwalletPropertiesListResponse response = PropertyListInfoFile.readFromInputStream(is)

        then:
        response.status == "OK"
        response.additionalProperties.blockheight == 630549
        response.propertyInfoList.size() >= 1314
    }
}
