package foundation.omni.test.rpc.basic

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem

import static foundation.omni.CurrencyID.MSC
import static foundation.omni.CurrencyID.TMSC
import foundation.omni.rpc.SmartPropertyListInfo

/**
 * Specification for listproperties_MP
 */
class ListPropertiesSpec extends BaseRegTestSpec {

    def "Returns a property list with correct MSC and TMSC entries"() {
        when: "we get a list of properties"
        List<SmartPropertyListInfo> properties = listproperties_MP()

        then: "we get a list of size >= 2"
        properties != null
        properties.size() >= 2

        when: "we convert the list to a map"
        // This may be unnecessary if we can assume the property list is ordered by propertyid
        Map<CurrencyID, SmartPropertyListInfo> props = properties.collect{[it.id, it]}.collectEntries()

        then: "we can check MSC and TMSC are as expected"
        props[MSC].id == MSC
        props[MSC].id.ecosystem == Ecosystem.MSC
        props[MSC].name == "MasterCoin"
        props[MSC].category == "N/A"
        props[MSC].subCategory == "N/A"
        props[MSC].data == "***data***"
        props[MSC].url == "www.mastercoin.org"
        props[MSC].divisible == true

        props[TMSC].id == TMSC
        props[TMSC].id.ecosystem == Ecosystem.TMSC
        props[TMSC].name == "Test MasterCoin"
        props[TMSC].category == "N/A"
        props[TMSC].subCategory == "N/A"
        props[TMSC].data == "***data***"
        props[TMSC].url == "www.mastercoin.org"
        props[TMSC].divisible == true
    }
}