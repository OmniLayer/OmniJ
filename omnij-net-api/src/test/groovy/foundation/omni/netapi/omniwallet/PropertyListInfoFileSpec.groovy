package foundation.omni.netapi.omniwallet


import foundation.omni.json.pojo.OmniPropertyInfo
import foundation.omni.netapi.omniwallet.json.OmniwalletPropertiesListResponse
import spock.lang.Specification

/**
 *  Test loading PropertyListInfo from a file
 */
class PropertyListInfoFileSpec extends Specification {
    def "load test file resource as response"() {
        given:
        InputStream is =  PropertyListInfoFileSpec.getResourceAsStream("/foundation/omni/netapi/omniwallet/omniwallet_property_list_w_blockheight.json")
        
        when:
        OmniwalletPropertiesListResponse response = PropertyInfoFromJsonFile.readResponseFromInputStream(is)

        then:
        response.status == "OK"
        response.additionalProperties.blockheight == 630549
        response.propertyInfoList.size() >= 1314
    }

    def "load test file resource as list"() {
        given:
        InputStream is =  PropertyListInfoFileSpec.getResourceAsStream("/foundation/omni/netapi/omniwallet/omniwallet_property_list_w_blockheight.json")

        when:
        List<OmniPropertyInfo> list = PropertyInfoFromJsonFile.readPropertyInfoListFromInputStream(is)

        then:
        list.size() >= 1314
    }

}
