package foundation.omni.test.rpc.basic

import foundation.omni.BaseRegTestSpec
import foundation.omni.CurrencyID
import foundation.omni.Ecosystem
import spock.lang.Ignore

import static foundation.omni.CurrencyID.OMNI
import static foundation.omni.CurrencyID.TOMNI
import foundation.omni.rpc.SmartPropertyListInfo

/**
 * Specification for {@code "omni_listproperties"}.
 */
@Ignore("due to incompatibility with Omni Core 0.0.10")
class ListPropertiesSpec extends BaseRegTestSpec {

    def "Returns a property list with correct MSC and TMSC entries"() {
        when: "we get a list of properties"
        List<SmartPropertyListInfo> properties = omniListProperties()

        then: "we get a list of size >= 2"
        properties != null
        properties.size() >= 2

        when: "we convert the list to a map"
        // This may be unnecessary if we can assume the property list is ordered by propertyid
        Map<CurrencyID, SmartPropertyListInfo> props = properties.collect{[it.propertyid, it]}.collectEntries()

        then: "we can check OMNI and TOMNI are as expected"
        props[OMNI].propertyid == OMNI
        props[OMNI].propertyid.ecosystem == Ecosystem.OMNI
        props[OMNI].name == "MasterCoin"
        props[OMNI].category == "N/A"
        props[OMNI].subcategory == "N/A"
        props[OMNI].data == "***data***"
        props[OMNI].url == "www.mastercoin.org"
        props[OMNI].divisible

        props[TOMNI].propertyid == TOMNI
        props[TOMNI].propertyid.ecosystem == Ecosystem.TOMNI
        props[TOMNI].name == "Test MasterCoin"
        props[TOMNI].category == "N/A"
        props[TOMNI].subcategory == "N/A"
        props[TOMNI].data == "***data***"
        props[TOMNI].url == "www.mastercoin.org"
        props[TOMNI].divisible
    }
}