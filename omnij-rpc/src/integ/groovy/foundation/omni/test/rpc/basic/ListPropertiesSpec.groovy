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
class ListPropertiesSpec extends BaseRegTestSpec {

    @Ignore('OMNI, TOMNI changed to OMN, TOMN')
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
        props[OMNI].name == "Omni"
        props[OMNI].category == "N/A"
        props[OMNI].subcategory == "N/A"
        props[OMNI].data == "Omni serve as the binding between Bitcoin, smart properties and contracts created on the Omni Layer."
        props[OMNI].url == "http://www.omnilayer.org"
        props[OMNI].divisible

        props[TOMNI].propertyid == TOMNI
        props[TOMNI].propertyid.ecosystem == Ecosystem.TOMNI
        props[TOMNI].name == "Test Omni"
        props[TOMNI].category == "N/A"
        props[TOMNI].subcategory == "N/A"
        props[TOMNI].data == "Test Omni serve as the binding between Bitcoin, smart properties and contracts created on the Omni Layer."
        props[TOMNI].url == "http://www.omnilayer.org"
        props[TOMNI].divisible
    }
}
